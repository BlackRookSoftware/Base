package com.blackrook.base.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import java.util.TreeMap;

/**
 * A bulk image manipulation utility class.
 * @author Matthew Tropiano
 */
public final class ImageUtils
{

	private ImageUtils() {} // Don't instantiate.
	
	/**
	 * Creates a new image.
	 * @param width the new image width.
	 * @param height the new image height. 
	 * @return a new image.
	 */
	public static BufferedImage image(int width, int height)
	{
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Creates an image filled with a color.
	 * @param color the color.
	 * @param width the new image width.
	 * @param height the new image height. 
	 * @return a new image.
	 */
	public static BufferedImage colorImage(Color color, int width, int height)
	{
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.setColor(color);
		g.fillRect(0, 0, out.getWidth(), out.getHeight());
		g.dispose();
		return out;
	}

	// =======================================================================

	/**
	 * Copies an image.
	 * @param source the source image.
	 * @return a new image.
	 */
	public static BufferedImage copy(BufferedImage source)
	{
		ColorModel cm = source.getColorModel();
	    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	    WritableRaster raster = source.copyData(source.getRaster().createCompatibleWritableRaster());
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	/**
	 * Resizes and returns a new image.
	 * @param source the source image.
	 * @param resamplingType the resampling type.
	 * @param width the new width.
	 * @param height the new height.
	 * @return a new image.
	 */
	public static BufferedImage resize(BufferedImage source, ResamplingType resamplingType, int width, int height)
	{
		if (width < 1)
			throw new IllegalArgumentException("width cannot be < 1");
		if (height < 1)
			throw new IllegalArgumentException("height cannot be < 1");
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		resamplingType.setHints(g);
		g.drawImage(source, 0, 0, width, height, null);
		g.dispose();
		return out;
	}
	
	/**
	 * Resizes and returns a new image.
	 * @param source the source image.
	 * @param resamplingType the resampling type.
	 * @param scaleX the X axis scalar.
	 * @param scaleY the Y axis scalar.
	 * @return a new image.
	 */
	public static BufferedImage scale(BufferedImage source, ResamplingType resamplingType, float scaleX, float scaleY)
	{
		int width = (int)(scaleX * source.getWidth());
		int height = (int)(scaleY * source.getHeight());
		if (width < 1)
			width = 1;
		if (height < 1)
			height = 1;
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		resamplingType.setHints(g);
		g.drawImage(source, 0, 0, width, height, null);
		g.dispose();
		return out;
	}
	
	/**
	 * Crops and returns a new image.
	 * @param source the source image.
	 * @param x the x origin (from top-left).
	 * @param y the y origin (from top-left).
	 * @param width the width in pixels.
	 * @param height the height in pixels.
	 * @return a new image.
	 */
	public static BufferedImage crop(BufferedImage source, int x, int y, int width, int height)
	{
		if (width < 1)
			throw new IllegalArgumentException("width cannot be < 1");
		if (height < 1)
			throw new IllegalArgumentException("height cannot be < 1");
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.drawImage(source, -x, -y, source.getWidth(), source.getHeight(), null);
		g.dispose();
		return out;
	}
	
	/**
	 * Flips an image horizontally and returns a new image.
	 * @param source the source image.
	 * @return a new image.
	 */
	public static BufferedImage flipH(BufferedImage source)
	{
		BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.drawImage(source, source.getWidth(), 0, -source.getWidth(), source.getHeight(), null);
		g.dispose();
		return out;
	}
	
	/**
	 * Flips an image vertically and returns a new image.
	 * @param source the source image.
	 * @return a new image.
	 */
	public static BufferedImage flipV(BufferedImage source)
	{
		BufferedImage out = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = out.createGraphics();
		g.drawImage(source, 0, source.getHeight(), source.getWidth(), -source.getHeight(), null);
		g.dispose();
		return out;
	}
	
	/**
	 * Transposes an image and returns a new image.
	 * @param source the source image.
	 * @return a new image.
	 */
	public static BufferedImage transpose(BufferedImage source)
	{
		BufferedImage out = new BufferedImage(source.getHeight(), source.getWidth(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < source.getWidth(); x++)
			for (int y = 0; y < source.getHeight(); y++)
				out.setRGB(y, x, source.getRGB(x, y));
		return out;
	}
	
	/**
	 * Paints an image into another image.
	 * @param source the source image to paint into.
	 * @param incoming the incoming image to paint.
	 * @param x the target X-coordinate (from top-left of image).
	 * @param y the target Y-coordinate (from top-left of image).
	 * @return the source image.
	 */
	public static BufferedImage paint(BufferedImage source, BufferedImage incoming, int x, int y)
	{
		return paint(source, incoming, x, y, incoming.getWidth(), incoming.getHeight(), ResamplingType.NEAREST, CompositingType.ALPHA, 1.0f);
	}
	
	/**
	 * Paints an image into another image.
	 * @param source the source image to paint into.
	 * @param incoming the incoming image to paint.
	 * @param x the target X-coordinate (from top-left of image).
	 * @param y the target Y-coordinate (from top-left of image).
	 * @param alpha the alpha scalar for the blend.
	 * @return the source image.
	 */
	public static BufferedImage paint(BufferedImage source, BufferedImage incoming, int x, int y, float alpha)
	{
		return paint(source, incoming, x, y, incoming.getWidth(), incoming.getHeight(), ResamplingType.NEAREST, CompositingType.ALPHA, alpha);
	}
	
	/**
	 * Paints an image into another image.
	 * @param source the source image to paint into.
	 * @param incoming the incoming image to paint.
	 * @param x the target X-coordinate (from top-left of image).
	 * @param y the target Y-coordinate (from top-left of image).
	 * @param blend the blending composite type for the paint.
	 * @param alpha the alpha scalar for the blend.
	 * @return the source image.
	 */
	public static BufferedImage paint(BufferedImage source, BufferedImage incoming, int x, int y, CompositingType blend, float alpha)
	{
		return paint(source, incoming, x, y, incoming.getWidth(), incoming.getHeight(), ResamplingType.NEAREST, blend, alpha);
	}
	
	/**
	 * Paints an image into another image.
	 * @param source the source image to paint into.
	 * @param incoming the incoming image to paint.
	 * @param x the target X-coordinate (from top-left of image).
	 * @param y the target Y-coordinate (from top-left of image).
	 * @param width the new width of the image to paint.
	 * @param height the new height of the image to paint.
	 * @param resamplingType the resampling type for the resize.
	 * @param blend the blending composite type for the paint.
	 * @param alpha the alpha scalar for the blend.
	 * @return the source image.
	 */
	public static BufferedImage paint(BufferedImage source, BufferedImage incoming, int x, int y, int width, int height, ResamplingType resamplingType, CompositingType blend, float alpha)
	{
		if (width < 1)
			throw new IllegalArgumentException("width cannot be < 1");
		if (height < 1)
			throw new IllegalArgumentException("height cannot be < 1");

		Graphics2D g = source.createGraphics();
		Composite oldComposite = blend.setComposite(g, alpha);
		resamplingType.setHints(g);
		g.drawImage(incoming, x, y, width, height, null);
		g.setComposite(oldComposite);
		g.dispose();
		return source;
	}
	
	// =======================================================================

	/**
	 * A function that takes two images and combines them, returning the resultant image.
	 */
	@FunctionalInterface
	public interface ImageFunction
	{
		/**
		 * Performs an action on an image returns the result as a new image.
		 * @param source the source image.
		 * @return a new image.
		 */
		BufferedImage performAction(BufferedImage source);
	}
	
	/**
	 * A function that takes two images and combines them, returning the resultant image.
	 */
	@FunctionalInterface
	public interface ImageCombinerFunction
	{
		/**
		 * Performs an action on an image with another image and returns the result as a new image.
		 * @param source the source image.
		 * @param incoming the incoming image.
		 * @return a new image.
		 */
		BufferedImage performAction(BufferedImage source, RenderedImage incoming);
	}
	
	
	/**
	 * Resampling types.
	 */
	public enum ResamplingType
	{
		NEAREST
		{
			@Override
			public void setHints(Graphics2D g)
			{
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		},
		
		LINEAR
		{
			@Override
			public void setHints(Graphics2D g)
			{
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
		},
		
		BILINEAR
		{
			@Override
			public void setHints(Graphics2D g)
			{
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
		},
		
		BICUBIC
		{
			@Override
			public void setHints(Graphics2D g)
			{
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
		},
		;
		
		
		/**
		 * Sets the rendering hints for this type.
		 * @param g the graphics context.
		 */
		public abstract void setHints(Graphics2D g);
		
		public static final Map<String, ResamplingType> VALUES = new TreeMap<String, ResamplingType>(String.CASE_INSENSITIVE_ORDER)
		{
			private static final long serialVersionUID = -6575715699170949164L;
			{
				for (ResamplingType type : ResamplingType.values())
				{
					put(type.name(), type);
				}
			}
		};
	}

	/**
	 * Compositing types.
	 */
	public enum CompositingType
	{
		REPLACE
		{
			@Override
			public Composite setComposite(Graphics2D g, float scalar)
			{
				Composite old = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
				return old;
			}
		},
		
		ALPHA
		{
			@Override
			public Composite setComposite(Graphics2D g, float scalar)
			{
				Composite old = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, scalar));
				return old;
			}
		},
		
		ADD
		{
			@Override
			public Composite setComposite(Graphics2D g, float scalar)
			{
				Composite old = g.getComposite();
				g.setComposite(AdditiveComposite.getInstance(scalar));
				return old;
			}
		},
		
		SUBTRACT
		{
			@Override
			public Composite setComposite(Graphics2D g, float scalar)
			{
				Composite old = g.getComposite();
				g.setComposite(SubtractiveComposite.getInstance(scalar));
				return old;
			}
		},
		
		MULTIPLY
		{
			@Override
			public Composite setComposite(Graphics2D g, float scalar)
			{
				Composite old = g.getComposite();
				g.setComposite(MultiplicativeComposite.getInstance(scalar));
				return old;
			}
		},
		
		/** @since 1.10.2.1 */
		DESATURATE
		{
			@Override
			public Composite setComposite(Graphics2D g, float scalar)
			{
				Composite old = g.getComposite();
				g.setComposite(DesaturationComposite.getInstance(scalar));
				return old;
			}
		},
		
		;
		
		/**
		 * Push the composite for this type.
		 * @param g the graphics context.
		 * @param scalar the applicative scalar value.
		 * @return the old composite.
		 */
		public abstract Composite setComposite(Graphics2D g, float scalar);
	
		public static final Map<String, CompositingType> VALUES = new TreeMap<String, CompositingType>(String.CASE_INSENSITIVE_ORDER)
		{
			private static final long serialVersionUID = 907874275883556484L;
			{
				for (CompositingType type : CompositingType.values())
				{
					put(type.name(), type);
				}
			}
		};
	}

	/**
	 * A composite that adds pixel color together.
	 * The scalar amount for the addition per pixel is taken from the alpha component.  
	 */
	public static final class AdditiveComposite implements Composite
	{
		private static final AdditiveComposite INSTANCE = new AdditiveComposite();
		
		private float scalar;
		
		private AdditiveComposite()
		{
			this.scalar = 1f;
		}
	
		private AdditiveComposite(float scalar)
		{
			this.scalar = scalar;
		}
	
		/**
		 * @return an instance of this composite.
		 */
		public static AdditiveComposite getInstance()
		{
			return INSTANCE;
		}
		
		/**
		 * @param scalar the applicative scalar.
		 * @return an instance of this composite.
		 */
		public static AdditiveComposite getInstance(float scalar)
		{
			return new AdditiveComposite(scalar);
		}
		
		@Override
		public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) 
		{
			return new AdditiveCompositeContext(srcColorModel, dstColorModel, scalar);
		}
		
	}

	/**
	 * A composite that subtracts pixel color.
	 * The scalar amount for the subtraction per pixel is taken from the alpha component.  
	 */
	public static final class SubtractiveComposite implements Composite
	{
		private static final SubtractiveComposite INSTANCE = new SubtractiveComposite();
		
		private float scalar;
		
		private SubtractiveComposite()
		{
			this.scalar = 1f;
		}
	
		private SubtractiveComposite(float scalar)
		{
			this.scalar = scalar;
		}
	
		/**
		 * @return an instance of this composite.
		 */
		public static SubtractiveComposite getInstance()
		{
			return INSTANCE;
		}
		
		/**
		 * @param scalar the applicative scalar.
		 * @return an instance of this composite.
		 */
		public static SubtractiveComposite getInstance(float scalar)
		{
			return new SubtractiveComposite(scalar);
		}
		
		@Override
		public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) 
		{
			return new SubtractiveCompositeContext(srcColorModel, dstColorModel, scalar);
		}
		
	}

	/**
	 * A composite that multiplies pixel color together.
	 * The scalar amount for the multiply per pixel is taken from the alpha component.  
	 */
	public static final class MultiplicativeComposite implements Composite
	{
		private static final MultiplicativeComposite INSTANCE = new MultiplicativeComposite();
		
		private float scalar;
	
		private MultiplicativeComposite()
		{
			this.scalar = 1f;
		}
	
		private MultiplicativeComposite(float scalar)
		{
			this.scalar = scalar;
		}
	
		/**
		 * @return an instance of this composite.
		 */
		public static MultiplicativeComposite getInstance()
		{
			return INSTANCE;
		}
		
		/**
		 * @param scalar the applicative scalar.
		 * @return an instance of this composite.
		 */
		public static MultiplicativeComposite getInstance(float scalar)
		{
			return new MultiplicativeComposite(scalar);
		}
		
		@Override
		public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) 
		{
			return new MultiplicativeCompositeContext(srcColorModel, dstColorModel, scalar);
		}
		
	}

	/**
	 * A composite that multiplies pixel color together.
	 * The scalar amount for the multiply per pixel is taken from the alpha component.  
	 * @since 1.10.2.1
	 */
	public static final class DesaturationComposite implements Composite
	{
		private static final DesaturationComposite INSTANCE = new DesaturationComposite();
		
		private float scalar;
	
		private DesaturationComposite()
		{
			this.scalar = 1f;
		}
	
		private DesaturationComposite(float scalar)
		{
			this.scalar = scalar;
		}
	
		/**
		 * @return an instance of this composite.
		 */
		public static DesaturationComposite getInstance()
		{
			return INSTANCE;
		}
		
		/**
		 * @param scalar the applicative scalar.
		 * @return an instance of this composite.
		 */
		public static DesaturationComposite getInstance(float scalar)
		{
			return new DesaturationComposite(scalar);
		}
		
		@Override
		public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) 
		{
			return new DesaturationCompositeContext(srcColorModel, dstColorModel, scalar);
		}
		
	}

	// =======================================================================
	// =======================================================================
	
	/**
	 * All composite contexts that mix two pixels together.
	 */
	private static abstract class ARGBCompositeContext implements CompositeContext
	{
		protected ColorModel srcColorModel; 
		protected ColorModel dstColorModel;
		protected int preAlpha;
		
		/**
		 * Creates a new context with the provided color models and hints.
		 * @param srcColorModel the color model of the source.
		 * @param dstColorModel the color model of the destination.
		 * @param preAlpha the alpha to pre-apply (0 to 1).
		 */
		protected ARGBCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel, float preAlpha)
		{
			this.srcColorModel = srcColorModel;
			this.dstColorModel = dstColorModel;
			this.preAlpha = (int)(preAlpha * 255);
		}
		
		/**
		 * Checks if a {@link Raster} is the correct data format for this compositing operation.
		 * @param colorModel the color model to check compatibility for.
		 * @param raster the Raster to check.
		 * @throws UnsupportedOperationException if the Raster's data type is not {@link DataBuffer#TYPE_INT}.
		 */
		protected static void checkRaster(ColorModel colorModel, Raster raster) 
		{
	        if (!colorModel.isCompatibleRaster(raster))
	            throw new UnsupportedOperationException("ColorModel is not compatible with raster.");
	        if (raster.getSampleModel().getDataType() != DataBuffer.TYPE_INT)
	            throw new UnsupportedOperationException("Expected integer data type from raster.");
	    }
		
		/**
		 * Mixes two pixels together.
		 * @param srcARGB the incoming ARGB 32-bit integer value.
		 * @param dstARGB the existing, "source" ARGB 32-bit integer value.
		 * @return the resultant ARGB value.
		 */
		protected abstract int composePixel(int srcARGB, int dstARGB);
		
		@Override
		public void compose(Raster src, Raster dstIn, WritableRaster dstOut)
		{
			// alpha of 0 = do nothing.
			if (preAlpha == 0)
				return;
			
			checkRaster(srcColorModel, src);
			checkRaster(dstColorModel, dstIn);
			checkRaster(dstColorModel, dstOut);
			
			int width = Math.min(src.getWidth(), dstIn.getWidth());
			int height = Math.min(src.getHeight(), dstIn.getHeight());
			int[] srcRowBuffer = new int[width];
			int[] dstRowBuffer = new int[width];
			
			for (int y = 0; y < height; y++) 
			{
				src.getDataElements(0, y, width, 1, srcRowBuffer);
				dstIn.getDataElements(0, y, width, 1, dstRowBuffer);
				
				for (int x = 0; x < width; x++)
					dstRowBuffer[x] = composePixel(srcColorModel.getRGB(srcRowBuffer[x]), dstColorModel.getRGB(dstRowBuffer[x]));
				
				dstOut.setDataElements(0, y, width, 1, dstRowBuffer);
			}
		}
	
		@Override
		public void dispose() 
		{
			this.srcColorModel = null;
			this.dstColorModel = null;
		}
	}

	/**
	 * The composite context for {@link AdditiveComposite}s. 
	 */
	public static class AdditiveCompositeContext extends ARGBCompositeContext
	{
		private AdditiveCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel, float preAlpha)
		{
			super(srcColorModel, dstColorModel, preAlpha);
		}
	
		@Override
		protected int composePixel(int srcARGB, int dstARGB) 
		{
			int srcBlue =  (srcARGB & 0x000000FF);
			int dstBlue =  (dstARGB & 0x000000FF);
			int srcGreen = (srcARGB & 0x0000FF00) >>> 8;
			int dstGreen = (dstARGB & 0x0000FF00) >>> 8;
			int srcRed =   (srcARGB & 0x00FF0000) >>> 16;
			int dstRed =   (dstARGB & 0x00FF0000) >>> 16;
			int srcAlpha = (srcARGB & 0xFF000000) >>> 24;
			int dstAlpha = (dstARGB & 0xFF000000) >>> 24;
	
			srcAlpha = (srcAlpha * preAlpha / 255);
			
			// Scale alpha.
			srcBlue =  srcBlue  * srcAlpha / 255;
			srcGreen = srcGreen * srcAlpha / 255;
			srcRed =   srcRed   * srcAlpha / 255;
	
			int outARGB = 0x00000000;
			outARGB |= Math.min(Math.max(dstBlue  + srcBlue,  0x000), 0x0FF);
			outARGB |= Math.min(Math.max(dstGreen + srcGreen, 0x000), 0x0FF) << 8;
			outARGB |= Math.min(Math.max(dstRed   + srcRed,   0x000), 0x0FF) << 16;
			outARGB |= dstAlpha << 24;
			return outARGB;
		}
	}

	/**
	 * The composite context for {@link SubtractiveComposite}s. 
	 */
	public static class SubtractiveCompositeContext extends ARGBCompositeContext
	{
		private SubtractiveCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel, float preAlpha)
		{
			super(srcColorModel, dstColorModel, preAlpha);
		}
	
		@Override
		protected int composePixel(int srcARGB, int dstARGB) 
		{
			int srcBlue =  (srcARGB & 0x000000FF);
			int dstBlue =  (dstARGB & 0x000000FF);
			int srcGreen = (srcARGB & 0x0000FF00) >>> 8;
			int dstGreen = (dstARGB & 0x0000FF00) >>> 8;
			int srcRed =   (srcARGB & 0x00FF0000) >>> 16;
			int dstRed =   (dstARGB & 0x00FF0000) >>> 16;
			int srcAlpha = (srcARGB & 0xFF000000) >>> 24;
			int dstAlpha = (dstARGB & 0xFF000000) >>> 24;
	
			srcAlpha = (srcAlpha * preAlpha / 255);
			
			// Scale alpha.
			srcBlue =  srcBlue  * srcAlpha / 255;
			srcGreen = srcGreen * srcAlpha / 255;
			srcRed =   srcRed   * srcAlpha / 255;
	
			int outARGB = 0x00000000;
			outARGB |= Math.min(Math.max(dstBlue  - srcBlue,  0x000), 0x0FF);
			outARGB |= Math.min(Math.max(dstGreen - srcGreen, 0x000), 0x0FF) << 8;
			outARGB |= Math.min(Math.max(dstRed   - srcRed,   0x000), 0x0FF) << 16;
			outARGB |= dstAlpha << 24;
			return outARGB;
		}
	}

	/**
	 * The composite context for {@link MultiplicativeComposite}s. 
	 */
	public static class MultiplicativeCompositeContext extends ARGBCompositeContext
	{
		protected MultiplicativeCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel, float preAlpha)
		{
			super(srcColorModel, dstColorModel, preAlpha);
		}
	
		@Override
		protected int composePixel(int srcARGB, int dstARGB) 
		{
			int srcBlue =  (srcARGB & 0x000000FF);
			int dstBlue =  (dstARGB & 0x000000FF);
			int srcGreen = (srcARGB & 0x0000FF00) >>> 8;
			int dstGreen = (dstARGB & 0x0000FF00) >>> 8;
			int srcRed =   (srcARGB & 0x00FF0000) >>> 16;
			int dstRed =   (dstARGB & 0x00FF0000) >>> 16;
			int srcAlpha = (srcARGB & 0xFF000000) >>> 24;
			int dstAlpha = (dstARGB & 0xFF000000) >>> 24;
			
			srcAlpha = (srcAlpha * preAlpha / 255);
			
			// Scale alpha.
			srcBlue =  srcBlue  + ((255 - srcBlue)  * (255 - srcAlpha) / 255);
			srcGreen = srcGreen + ((255 - srcGreen) * (255 - srcAlpha) / 255);
			srcRed =   srcRed   + ((255 - srcRed)   * (255 - srcAlpha) / 255);
	
			int outARGB = 0x00000000;
			outARGB |= (dstBlue  * srcBlue  / 255);
			outARGB |= (dstGreen * srcGreen / 255) << 8;
			outARGB |= (dstRed   * srcRed   / 255) << 16;
			outARGB |= dstAlpha << 24;
			return outARGB;
		}
	}

	/**
	 * The composite context for {@link DesaturationComposite}s.
	 * @since 1.10.2.1
	 */
	public static class DesaturationCompositeContext extends ARGBCompositeContext
	{
		protected DesaturationCompositeContext(ColorModel srcColorModel, ColorModel dstColorModel, float preAlpha)
		{
			super(srcColorModel, dstColorModel, preAlpha);
		}
	
		@Override
		protected int composePixel(int srcARGB, int dstARGB) 
		{
			int srcRed =   (srcARGB & 0x00FF0000) >>> 16;
			int srcAlpha = (srcARGB & 0xFF000000) >>> 24;
	
			int dstBlue =  (dstARGB & 0x000000FF);
			int dstGreen = (dstARGB & 0x0000FF00) >>> 8;
			int dstRed =   (dstARGB & 0x00FF0000) >>> 16;
			int dstAlpha = (dstARGB & 0xFF000000) >>> 24;
			
			srcAlpha = (srcAlpha * preAlpha / 255);
			
			int dstLum = (dstBlue * 19 / 255) + (dstGreen * 182 / 255) + (dstRed * 54 / 255);
			int srcDesat = srcRed * srcAlpha / 255;
			
			int outARGB = 0x00000000;
			outARGB |= mix(dstBlue,  dstLum, srcDesat);
			outARGB |= mix(dstGreen, dstLum, srcDesat) << 8;
			outARGB |= mix(dstRed,   dstLum, srcDesat) << 16;
			outARGB |= dstAlpha << 24;
			return outARGB;
		}
	
		private static int mix(int a, int b, int mix)
		{
			return (((255 - mix) * a) + (mix * b)) / 255;
		}
	}
	
}
