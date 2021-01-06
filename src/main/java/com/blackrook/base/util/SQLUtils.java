/*******************************************************************************
 * Copyright (c) 2020 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package com.blackrook.base.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Slim SQL utility class.
 * This is a truncated version of Black Rook SQL - no reflection.
 * @author Matthew Tropiano
 */
public final class SQLUtils 
{
	/** Default batch size. */
	public static final int DEFAULT_BATCH_SIZE = 1024;

	/**
	 * Performs a query on a connection and extracts the data into a single {@link Row}.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameters list of parameters for parameterized queries.
	 * @return the single result row returned, or null if no row returned.
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @see #getResult(Connection, String, Object...)
	 */
	public static Row getRow(Connection connection, String query, Object ... parameters) throws SQLException
	{
		return getResult(connection, query, parameters).getRow();
	}

	/**
	 * Performs a query on a connection and extracts the data into a {@link Result}.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameters list of parameters for parameterized queries.
	 * @return the result of the query.
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 */
	public static Result getResult(Connection connection, String query, Object ... parameters) throws SQLException
	{
		try (PreparedStatement statement = connection.prepareStatement(query))
		{
			return callStatement(statement, false, parameters);
		}
	}

	/**
	 * Performs an update query (INSERT, DELETE, UPDATE, or other commands that do not return rows)
	 * on a connection and extracts the data/affected data/generated data into a {@link Result}.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameters list of parameters for parameterized queries.
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 */
	public static Result getUpdateResult(Connection connection, String query, Object ... parameters) throws SQLException
	{
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			return callStatement(statement, true, parameters);
		}
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	public static int[] getUpdateBatch(Connection connection, String query, Object[][] parameterList) throws SQLException
	{
		return getUpdateBatch(connection, query, DEFAULT_BATCH_SIZE, Arrays.asList(parameterList));
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param granularity the amount of statements to execute at a time. If 0 or less, no granularity.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	public static int[] getUpdateBatch(Connection connection, String query, int granularity, Object[][] parameterList) throws SQLException
	{
		return getUpdateBatch(connection, query, granularity, Arrays.asList(parameterList));
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	public static int[] getUpdateBatch(Connection connection, String query, Collection<Object[]> parameterList) throws SQLException
	{
		return getUpdateBatch(connection, query, DEFAULT_BATCH_SIZE, parameterList);
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param granularity the amount of statements to execute at a time. If 0 or less, no granularity.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	public static int[] getUpdateBatch(Connection connection, String query, int granularity, Collection<Object[]> parameterList) throws SQLException
	{
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			return callBatch(statement, granularity, parameterList);
		}
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	public static long[] getUpdateLargeBatch(Connection connection, String query, Object[][] parameterList) throws SQLException
	{
		return getUpdateLargeBatch(connection, query, DEFAULT_BATCH_SIZE, Arrays.asList(parameterList));
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param granularity the amount of statements to execute at a time. If 0 or less, no granularity.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	public static long[] getUpdateLargeBatch(Connection connection, String query, int granularity, Object[][] parameterList) throws SQLException
	{
		return getUpdateLargeBatch(connection, query, granularity, Arrays.asList(parameterList));
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	public static long[] getUpdateLargeBatch(Connection connection, String query, Collection<Object[]> parameterList) throws SQLException
	{
		return getUpdateLargeBatch(connection, query, DEFAULT_BATCH_SIZE, parameterList);
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param granularity the amount of statements to execute at a time. If 0 or less, no granularity.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the update result returned (usually number of rows affected and or generated ids).
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	public static long[] getUpdateLargeBatch(Connection connection, String query, int granularity, Collection<Object[]> parameterList) throws SQLException
	{
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			return callLargeBatch(statement, granularity, parameterList);
		}
	}

	/**
	 * Performs an update query (INSERT, DELETE, UPDATE, or other commands that do not return rows)
	 * and extracts each set of result data into a {@link Result}.
	 * <p>This is usually more efficient than multiple calls of {@link #getUpdateResult(Connection, String, Object...)},
	 * since it uses the same prepared statement. However, it is not as efficient as 
	 * {@link #getUpdateBatch(Connection, String, int, Collection)} or {@link #getUpdateLargeBatch(Connection, String, int, Collection)},
	 * but for this method, you will get the generated ids in each result, if any.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the list of update results returned, each corresponding to an update.
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 */
	public static Result[] getUpdateBatchResult(Connection connection, String query, Object[][] parameterList) throws SQLException
	{
		return getUpdateBatchResult(connection, query, Arrays.asList(parameterList));
	}

	/**
	 * Performs an update query (INSERT, DELETE, UPDATE, or other commands that do not return rows)
	 * and extracts each set of result data into a {@link Result}.
	 * <p>This is usually more efficient than multiple calls of {@link #getUpdateResult(Connection, String, Object...)},
	 * since it uses the same prepared statement. However, it is not as efficient as {@link #getUpdateBatch(Connection, String, int, Collection)},
	 * but for this method, you will get the generated ids in each result, if any.
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param query the query statement to execute.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the list of update results returned, each corresponding to an update.
	 * @throws SQLException if the query cannot be executed or the query causes an error.
	 */
	public static Result[] getUpdateBatchResult(Connection connection, String query, Collection<Object[]> parameterList) throws SQLException
	{
		try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS))
		{
			int i = 0;
			Result[] out = new Result[parameterList.size()];
			for (Object[] params : parameterList)
				out[i++] = callStatement(statement, true, params);
			return out;
		}
	}

	/**
	 * Starts a transaction with a provided level.
	 * <p>The connection gets {@link Connection#setAutoCommit(boolean)} called on it with a FALSE parameter,
	 * and sets the transaction isolation level. These settings are restored when the transaction is 
	 * finished via {@link Transaction#close()}, {@link Transaction#commit()}, or {@link Transaction#rollback()}.
	 * <p>It is recommended to use an auto-closing mechanism to ensure that the transaction is completed and the connection transaction
	 * state is restored, or else this connection will be left in a bad state!
	 * @param connection the connection to create a prepared statement and execute from.
	 * @param transactionLevel the transaction level to set on this transaction.
	 * @return a new transaction.
	 * @throws SQLException if this transaction could not be prepared.
	 */
	public static Transaction getTransaction(Connection connection, TransactionLevel transactionLevel) throws SQLException
	{
		return new Transaction(connection, transactionLevel);
	}

	/**
	 * Performs a query on a connection and extracts the data into a Result object.
	 * @param statement the statement to execute.
	 * @param update if true, this is an update query. If false, it is a result query.
	 * @param parameters the parameters to pass to the 
	 * @return the query result returned.
	 * @throws SQLException if a SQL exception occurs.
	 */
	private static Result callStatement(PreparedStatement statement, boolean update, Object ... parameters) throws SQLException
	{
		Result out = null;
	
		int i = 1;
		for (Object obj : parameters)
			statement.setObject(i++, obj);
		
		if (update)
		{
			int rows = statement.executeUpdate();
			try (ResultSet resultSet = statement.getGeneratedKeys())
			{
				out = createResult(resultSet, true, rows);
			}
		}
		else
		{
			try (ResultSet resultSet = statement.executeQuery())
			{
				out = createResult(resultSet, false, -1);
			}
		}
		return out;
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param statement the statement to execute.
	 * @param granularity the amount of statements to execute at a time. If 0 or less, no granularity.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the amount of affected rows of each of the updates, each index corresponding to the index of the set of parameters used.
	 * 		May also return {@link Statement#SUCCESS_NO_INFO} or {@link Statement#EXECUTE_FAILED} per update.
	 * @throws SQLException if a SQL exception occurs.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	private static int[] callBatch(PreparedStatement statement, int granularity, Collection<Object[]> parameterList) throws SQLException
	{
		int[] out = new int[parameterList.size()];
		int cursor = 0;
		int batch = 0;
	
		for (Object[] parameters : parameterList)
		{
			int n = 1;
			for (Object obj : parameters)
				statement.setObject(n++, obj);
			
			statement.addBatch();
			batch++;
			
			if (batch == granularity)
			{
				int[] execute = statement.executeBatch();
				System.arraycopy(execute, 0, out, cursor, execute.length);
				cursor += execute.length;
				batch = 0;
			}
		}
		
		if (batch != 0)
		{
			int[] execute = statement.executeBatch();
			System.arraycopy(execute, 0, out, cursor, execute.length);
		}
		
		return out;
	}

	/**
	 * Performs a series of update queries on a single statement on a connection and returns the batch result.
	 * @param statement the statement to execute.
	 * @param granularity the amount of statements to execute at a time. If 0 or less, no granularity.
	 * @param parameterList the list of parameter sets to pass to the query for each update. 
	 * @return the amount of affected rows of each of the updates, each index corresponding to the index of the set of parameters used.
	 * 		May also return {@link Statement#SUCCESS_NO_INFO} or {@link Statement#EXECUTE_FAILED} per update.
	 * @throws SQLException if a SQL exception occurs.
	 * @throws UnsupportedOperationException if not implemented by the driver.
	 */
	private static long[] callLargeBatch(PreparedStatement statement, int granularity, Collection<Object[]> parameterList) throws SQLException
	{
		long[] out = new long[parameterList.size()];
		int cursor = 0;
		int batch = 0;
	
		for (Object[] parameters : parameterList)
		{
			int n = 1;
			for (Object obj : parameters)
				statement.setObject(n++, obj);
			
			statement.addBatch();
			batch++;
			
			if (batch == granularity)
			{
				long[] execute = statement.executeLargeBatch();
				System.arraycopy(execute, 0, out, cursor, execute.length);
				cursor += execute.length;
				batch = 0;
			}
		}
		
		if (batch != 0)
		{
			long[] execute = statement.executeLargeBatch();
			System.arraycopy(execute, 0, out, cursor, execute.length);
		}
		
		return out;
	}

	/**
	 * Creates a {@link Result} from a result set.
	 * The result set is assumed to be at the beginning of the set.
	 */
	private static Result createResult(ResultSet resultSet, boolean update, int rows) throws SQLException
	{
		Result out = null;
	
		if (update)
			out = new Result(rows, resultSet);
		else
			out = new Result(resultSet);
		
		return out;
	}

	/**
	 * Returns the names of the columns in a ResultSet in the order that they appear in the result.
	 * @param set the ResultSet to get the columns from.
	 * @return an array of all of the columns.
	 * @throws SQLException if something goes wrong.
	 */
	private static String[] getAllColumnNamesFromResultSet(ResultSet set) throws SQLException
	{
		ResultSetMetaData md = set.getMetaData();
		String[] out = new String[md.getColumnCount()];
		for (int i = 0; i < out.length; i++)
			out[i] = md.getColumnName(i+1);
		return out;
	}

	/**
	 * Row object. 
	 * Represents one row in a query result, mapped using column names.
	 * Contains methods for auto-casting or converting the row data.
	 */
	public static class Row implements Iterable<Map.Entry<String, Object>>
	{
		private static final ThreadLocal<char[]> CHARBUFFER = ThreadLocal.withInitial(()->new char[1024 * 8]);
		private static final ThreadLocal<byte[]> BYTEBUFFER = ThreadLocal.withInitial(()->new byte[1024 * 32]);
		
		/** Column index to SQL object. */
		private List<Object> columnList;
		/** Map of column name to index. */
		private Map<String, Integer> columnMap;
		
		/**
		 * Constructor for a SQL row.
		 * @param rs the open {@link ResultSet}, set to the row to create a Row from.
		 * @param columnNames the names given to the columns in the {@link ResultSet}, gathered ahead of time.
		 * @throws SQLException if a parse exception occurred.
		 */
		private Row(ResultSet rs, String[] columnNames) throws SQLException
		{
			this.columnList = new ArrayList<>(rs.getMetaData().getColumnCount());
			this.columnMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for (int i = 0; i < columnNames.length; i++)
			{
				Object sqlobj = rs.getObject(i + 1); // 1-based
				
				// Blobs, Clobs, and NClobs need to be converted while the connection is open.
				if (sqlobj instanceof Blob)
					sqlobj = unBlob((Blob)sqlobj);
				else if (sqlobj instanceof Clob)
					sqlobj = unClob((Clob)sqlobj);
				
				columnList.add(sqlobj);
				columnMap.put(columnNames[i], i);
			}
		}
		
		// Get a column by index.
		private Object getByIndex(Integer columnIndex)
		{
			if (columnIndex == null || columnIndex < 0 || columnIndex >= columnList.size())
				return null;
			return columnList.get(columnIndex);
		}

		// Get a column by name.
		private Object getByName(String columnName)
		{
			return getByIndex(columnMap.get(columnName));
		}
		
		/**
		 * Gets if a column's value is null.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public boolean getNull(int columnIndex)
		{
			return getByIndex(columnIndex) == null;
		}

		/**
		 * Gets if a column's value is null.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public boolean getNull(String columnName)
		{
			return getByName(columnName) == null;
		}

		/**
		 * Gets the boolean value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public boolean getBoolean(int columnIndex)
		{
			return getBoolean(getByIndex(columnIndex));
		}

		/**
		 * Gets the boolean value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public boolean getBoolean(String columnName)
		{
			return getBoolean(getByName(columnName));
		}
		
		/**
		 * Gets the byte value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public byte getByte(int columnIndex)
		{
			return getByte(getByIndex(columnIndex));
		}
		
		/**
		 * Gets the byte value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public byte getByte(String columnName)
		{
			return getByte(getByName(columnName));
		}
		
		/**
		 * Gets the byte array value of a column, if this
		 * can be represented as such (usually {@link Blob}s).
		 * Can convert from Blobs.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public byte[] getByteArray(int columnIndex)
		{
			return getByteArray(getByIndex(columnIndex));
		}
		
		/**
		 * Gets the byte array value of a column, if this
		 * can be represented as such (usually {@link Blob}s).
		 * Can convert from Blobs.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public byte[] getByteArray(String columnName)
		{
			return getByteArray(getByName(columnName));
		}

		/**
		 * Gets the short value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public short getShort(int columnIndex)
		{
			return getShort(getByIndex(columnIndex));
		}
		
		/**
		 * Gets the short value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public short getShort(String columnName)
		{
			return getShort(getByName(columnName));
		}

		/**
		 * Gets the integer value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public int getInt(int columnIndex)
		{
			return getInt(getByIndex(columnIndex));		
		}
		
		/**
		 * Gets the integer value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public int getInt(String columnName)
		{
			return getInt(getByName(columnName));
		}

		/**
		 * Gets the float value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public float getFloat(int columnIndex)
		{
			return getFloat(getByIndex(columnIndex));
		}

		/**
		 * Gets the float value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public float getFloat(String columnName)
		{
			return getFloat(getByName(columnName));
		}

		/**
		 * Gets the long value of a column.
		 * Can convert from Booleans, Numbers, Strings, and Dates/Timestamps.
		 * Booleans convert to 1 if true, 0 if false.
		 * Dates and Timestamps convert to milliseconds since the Epoch.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public long getLong(int columnIndex)
		{
			return getLong(getByIndex(columnIndex));
		}

		/**
		 * Gets the long value of a column.
		 * Can convert from Booleans, Numbers, Strings, and Dates/Timestamps.
		 * Booleans convert to 1 if true, 0 if false.
		 * Dates and Timestamps convert to milliseconds since the Epoch.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public long getLong(String columnName)
		{
			return getLong(getByName(columnName));
		}

		/**
		 * Gets the double value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public double getDouble(int columnIndex)
		{
			return getDouble(getByIndex(columnIndex));
		}
		
		/**
		 * Gets the double value of a column.
		 * Can convert from Booleans, Numbers, and Strings.
		 * Booleans convert to 1 if true, 0 if false.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public double getDouble(String columnName)
		{
			return getDouble(getByName(columnName));
		}

		/**
		 * Gets the string value of a column.
		 * Can convert from Booleans, Numbers, byte and char arrays, Blobs, and Clobs.
		 * Booleans convert to 1 if true, 0 if false.
		 * Byte arrays and Blobs are converted using the native charset encoding.
		 * Char arrays and Clobs are read entirely and converted to Strings.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 * @see String#valueOf(Object)
		 */
		public String getString(int columnIndex)
		{
			return getString(getByIndex(columnIndex));
		}

		/**
		 * Gets the string value of a column.
		 * Can convert from Booleans, Numbers, byte and char arrays, Blobs, and Clobs.
		 * Booleans convert to 1 if true, 0 if false.
		 * Byte arrays and Blobs are converted using the native charset encoding.
		 * Char arrays and Clobs are read entirely and converted to Strings.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 * @see String#valueOf(Object)
		 */
		public String getString(String columnName)
		{
			return getString(getByName(columnName));
		}

		/**
		 * Gets the Timestamp value of the object, or null if not a Timestamp or Date.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public Timestamp getTimestamp(int columnIndex)
		{
			return getTimestamp(getByIndex(columnIndex));
		}

		/**
		 * Gets the Timestamp value of the object, or null if not a Timestamp or Date.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public Timestamp getTimestamp(String columnName)
		{
			return getTimestamp(getByName(columnName));
		}

		/**
		 * Gets the Date value of the object, or null if not a Date.
		 * @param columnIndex the column index to read (0-based).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public Date getDate(int columnIndex)
		{
			return getDate(getByIndex(columnIndex));
		}
		
		/**
		 * Gets the Date value of the object, or null if not a Date.
		 * @param columnName the column name to read (case-insensitive).
		 * @return the resultant value, or null if not a valid column name.
		 */
		public Date getDate(String columnName)
		{
			return getDate(getByName(columnName));
		}

		@Override
		public Iterator<Entry<String, Object>> iterator() 
		{
			return new RowIterator(columnMap.entrySet().iterator());
		}

		private byte[] unBlob(Blob blob) throws SQLException
		{
			ByteArrayOutputStream bos = null;
			try (InputStream in = blob.getBinaryStream()) 
			{
				bos = new ByteArrayOutputStream();
				byte[] buffer = BYTEBUFFER.get();
				int buf = 0;
				while ((buf = in.read(buffer)) > 0)
					bos.write(buffer, 0, buf);
			} 
			catch (IOException e)
			{
				throw new SQLException("Could not read Blob.");
			}
			return bos.toByteArray();
		}

		private String unClob(Clob clob) throws SQLException
		{
			StringWriter sw = null;
			try (Reader reader = clob.getCharacterStream()) 
			{
				sw = new StringWriter();
				char[] charBuffer = CHARBUFFER.get();
				int cbuf = 0;
				while ((cbuf = reader.read(charBuffer)) > 0)
					sw.write(charBuffer, 0, cbuf);
			} 
			catch (IOException e)
			{
				throw new SQLException("Could not read Clob.");
			}
			return sw.toString();
		}
		
		private boolean getBoolean(Object obj)
		{
			if (obj == null)
				return false;
			else if (obj instanceof Boolean)
				return (Boolean)obj;
			else if (obj instanceof Number)
				return ((Number)obj).doubleValue() != 0.0f;
			else if (obj instanceof String)
				return Boolean.parseBoolean((String)obj);
			return false;
		}

		private byte getByte(Object obj)
		{
			if (obj == null)
				return (byte)0;
			else if (obj instanceof Boolean)
				return ((Boolean)obj) ? (byte)1 : (byte)0;
			else if (obj instanceof Number)
				return ((Number)obj).byteValue();
			else if (obj instanceof String)
				return Byte.parseByte((String)obj);
			return (byte)0;
		}

		private byte[] getByteArray(Object obj)
		{
			if (obj == null)
				return null;
			else if (obj.getClass().getComponentType() == Byte.TYPE)
				return (byte[])obj;
			return null;
		}

		private short getShort(Object obj)
		{
			if (obj == null)
				return (short)0;
			else if (obj instanceof Boolean)
				return ((Boolean)obj) ? (short)1 : (short)0;
			else if (obj instanceof Number)
				return ((Number)obj).shortValue();
			else if (obj instanceof String)
				return Short.parseShort((String)obj);
			return (short)0;
		}

		private int getInt(Object obj)
		{
			if (obj == null)
				return 0;
			else if (obj instanceof Boolean)
				return ((Boolean)obj) ? 1 : 0;
			else if (obj instanceof Number)
				return ((Number)obj).intValue();
			else if (obj instanceof String)
				return Integer.parseInt((String)obj);
			return 0;
		}

		private float getFloat(Object obj)
		{
			if (obj == null)
				return 0f;
			else if (obj instanceof Boolean)
				return ((Boolean)obj) ? 1f : 0f;
			else if (obj instanceof Number)
				return ((Number)obj).floatValue();
			else if (obj instanceof String)
				return Float.parseFloat((String)obj);
			return 0f;
		}

		private long getLong(Object obj)
		{
			if (obj == null)
				return 0L;
			else if (obj instanceof Boolean)
				return ((Boolean)obj) ? 1L : 0L;
			else if (obj instanceof Number)
				return ((Number)obj).longValue();
			else if (obj instanceof String)
				return Long.parseLong((String)obj);
			else if (obj instanceof Date)
				return ((Date)obj).getTime();
			return 0L;
		}

		private double getDouble(Object obj)
		{
			if (obj == null)
				return 0.0;
			else if (obj instanceof Boolean)
				return ((Boolean)obj) ? 1.0 : 0.0;
			else if (obj instanceof Number)
				return ((Number)obj).doubleValue();
			else if (obj instanceof String)
				return Double.parseDouble((String)obj);
			return 0.0;
		}

		private String getString(Object obj)
		{
			return obj != null ? String.valueOf(obj) : null;
		}

		private Timestamp getTimestamp(Object obj)
		{
			if (obj instanceof Timestamp)
				return ((Timestamp)obj);
			else if (obj instanceof Date)
				return new Timestamp(((Date)obj).getTime());
			return null;
		}

		private Date getDate(Object obj)
		{
			if (obj instanceof Date)
				return (Date)obj;
			return null;
		}
		
		private class RowIterator implements Iterator<Map.Entry<String, Object>>
		{
			private Iterator<Map.Entry<String, Integer>> columnIterator;
			
			private RowIterator(Iterator<Map.Entry<String, Integer>> iter)
			{
				this.columnIterator = iter;
			}
			
			@Override
			public boolean hasNext() 
			{
				return columnIterator.hasNext();
			}

			@Override
			public Map.Entry<String, Object> next()
			{
				Map.Entry<String, Integer> next = columnIterator.next();
				return new AbstractMap.SimpleEntry<>(next.getKey(), columnList.get(next.getValue()));
			}
		}
	}

	/**
	 * The data encapsulation of the result of a query {@link java.sql.ResultSet}.
	 */
	public static class Result implements Iterable<Row>
	{
		private static final String[] EMPTY_ARRAY = new String[0];
		
		/** Query Columns. */
		protected String[] columnNames;
		/** Rows affected or returned in the query. */
		protected int rowCount;
		/** Next id, if generated. */
		protected Object[] nextId;
		/** Was this an update query? */
		protected boolean update;
		/** List of rows of associative data. */
		protected List<Row> rows;
		
		/**
		 * Creates a new query result from an update query, plus generated keys. 
		 */
		private Result(int rowsAffected, ResultSet genKeys) throws SQLException
		{
			this.columnNames = EMPTY_ARRAY;
			this.update = true;
			this.rowCount = rowsAffected;
			this.rows = null;
			
			List<Object> vect = new ArrayList<Object>();
			while (genKeys.next())
				vect.add(genKeys.getLong(1));
			
			this.nextId = new Object[vect.size()];
			int x = 0;
			for (Object obj : vect)
				this.nextId[x++] = obj; 
		}

		/**
		 * Creates a new query result from a result set. 
		 */
		private Result(ResultSet rs) throws SQLException
		{
			this.columnNames = getAllColumnNamesFromResultSet(rs);
			this.update = false;
			this.rowCount = 0;
			this.rows = new ArrayList<Row>();
			
			while (rs.next())
			{
				this.rows.add(new Row(rs, columnNames));
				this.rowCount++;
			}
		}
		
		/**
		 * Gets the names of the columns.
		 * @return the column names in this result.
		 */
		public String[] getColumnNames()
		{
			return columnNames;
		}

		/**
		 * Gets the amount of affected/returned rows from this query. 
		 * @return the amount of records in this result.
		 */
		public int getRowCount()
		{
			return rowCount;
		}

		/**
		 * @return true if this came from an update, false otherwise.
		 */
		public boolean isUpdate()
		{
			return update;
		}
		
		/**
		 * Retrieves the rows from the query result.
		 * @return a list of the rows in this result.
		 */
		public List<Row> getRows()
		{
			return rows;
		}

		/**
		 * Gets the first row, or only row in this result, or null if no rows.
		 * @return the row in this result.
		 */
		public Row getRow()
		{
			return rows.size() > 0 ? rows.get(0) : null;
		}
		
		/**
		 * @return the generated id from the last query, if any, or null if none.
		 */
		public Object getId()
		{
			return nextId.length > 0 ? nextId[0] : null;
		}
		
		/**
		 * @return the list of generated ids from the last query.
		 */
		public Object[] getIds()
		{
			return nextId;
		}
		
		@Override
		public SQLResultIterator iterator()
		{
			return new SQLResultIterator();
		}

		/**
		 * The iterator returned to iterate through this result. 
		 */
		public class SQLResultIterator implements Iterator<Row>
		{
			private int current;
			
			public void reset()
			{
				current = 0;
			}
			
			@Override
			public boolean hasNext()
			{
				return current < rows.size();
			}

			@Override
			public Row next()
			{
				return rows.get(current++);
			}	
		}
		
	}

	/** 
	 * Enumeration of transaction levels. 
	 */
	public enum TransactionLevel
	{
		/**
		 * From {@link Connection}: A constant indicating that dirty reads are 
		 * prevented; non-repeatable reads and phantom reads can occur. This 
		 * level only prohibits a transaction from reading a row with uncommitted 
		 * changes in it.
		 */
		READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
	
		/**
		 * From {@link Connection}: A constant indicating that dirty reads, 
		 * non-repeatable reads and phantom reads can occur. This level allows 
		 * a row changed by one transaction to be read by another transaction 
		 * before any changes in that row have been committed (a "dirty read"). 
		 * If any of the changes are rolled back, the second transaction will 
		 * have retrieved an invalid row.
		 */
		READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
		
		/**
		 * From {@link Connection}: A constant indicating that dirty reads and 
		 * non-repeatable reads are prevented; phantom reads can occur. 
		 * This level prohibits a transaction from reading a row with 
		 * uncommitted changes in it, and it also prohibits the situation 
		 * where one transaction reads a row, a second transaction alters 
		 * the row, and the first transaction rereads the row, getting different 
		 * values the second time (a "non-repeatable read").
		 */
		REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
		
		/**
		 * From {@link Connection}: A constant indicating that dirty reads, 
		 * non-repeatable reads and phantom reads are prevented. This level 
		 * includes the prohibitions in TRANSACTION_REPEATABLE_READ and further 
		 * prohibits the situation where one transaction reads all rows that 
		 * satisfy a WHERE condition, a second transaction inserts a row that 
		 * satisfies that WHERE condition, and the first transaction rereads for 
		 * the same condition, retrieving the additional "phantom" row in the 
		 * second read.
		 */
		SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),
		;
		
		private final int id;
		private TransactionLevel(int id)
		{
			this.id = id;
		}
	}

	/**
	 * A transaction encapsulation.
	 * Committing or rolling back MUST be called through this, so that an auto-rollback is not
	 * triggered via {@link #close()}.
	 */
	public static class Transaction implements AutoCloseable
	{
		private Connection connection;
		private int previousLevelState;
		private boolean previousAutoCommit;
		private boolean finished;

		private Transaction(Connection connection, TransactionLevel transactionLevel) throws SQLException
		{
			this.connection = connection;
			this.previousLevelState = connection.getTransactionIsolation();
			this.previousAutoCommit = connection.getAutoCommit();
			this.finished = false;
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(transactionLevel.id);
		}

		/**
		 * Commits the actions completed so far in this transaction.
		 * @throws IllegalStateException if this transaction was already committed/rolled back.
		 * @throws SQLException if this causes a database error.
		 */
		public void commit() throws SQLException
		{
			if (finished)
				throw new IllegalStateException("This transaction is already finished.");
			connection.commit();
			finished = true;
		}

		/**
		 * Rolls back this entire transaction.
		 * @throws IllegalStateException if this transaction was already committed/rolled back.
		 * @throws SQLException if this causes a database error.
		 */
		public void rollback() throws SQLException
		{
			if (finished)
				throw new IllegalStateException("This transaction is already finished.");
			connection.rollback();
			finished = true;
		}

		/**
		 * If this transaction is not finished, this aborts it.
		 * The connection's isolation and "auto-commit" properties are restored.
		 */
		@Override
		public void close() throws SQLException
		{
			if (!finished)
				connection.rollback();
			connection.setTransactionIsolation(previousLevelState);
			connection.setAutoCommit(previousAutoCommit);
		}
	}
}
