# Colors

This Java library provides color space conversions and other color related utilities.

- **Color space conversions** 30+, bidirectional
- **Gamut management** for multiple display standards
- **RGBW/RGBWW mixing** for LED systems
- **Color harmony** complementary, triadic, analogous
- **Industry standard color spaces** video, broadcasting, printing
- **Color difference** Delta E 2000, MacAdam steps, WCAG contrast

## Color Space Conversions

### RGB-Based Color Spaces

#### Standard Color Models
- RGB
- Linear RGB
- HSV (Hue, Saturation, Value)
- HSL (Hue, Saturation, Lightness)  
- HSI (Hue, Saturation, Intensity)
- IHS (Intensity, Hue, Saturation)

#### CIE Color Spaces
- Lab (CIELAB)
- Luv (CIELUV)
- LCh (Lab-based cylindrical)
- LCHuv (Luv-based cylindrical)
- uv (CIE 1960 UCS)
- u'v' (CIE 1976 UCS)
- XYZ (CIE 1931 XYZ)
- xy (CIE 1931 chromaticity)
- xyY (CIE 1931 xyY)

#### Perceptually Uniform Spaces
- Oklab (Modern perceptually uniform color space)
- Oklch (Cylindrical version of Oklab)
- Okhsv (Oklab-based HSV)
- Okhsl (Oklab-based HSL)
- HSLuv (Human-friendly perceptual HSL)

#### Video & Broadcasting
- YCbCr (Digital video, supports ITU BT.601 and BT.709)
- YUV
- YIQ (NTSC)
- YCoCg
- YCC (Kodak Photo CD)
- YES (Xerox YES)

#### Printing & Other Spaces
- CMYK (Cyan, Magenta, Yellow, Key/Black)
- Hunter Lab
- LMS (Long, Medium, Short cone response)
- C1C2C3
- O1O2
- RG Chromaticity

### Example Usage
It is convenient to omit `Colors.` using a static import:
```java
import static com.esotericsoftware.Colors.*;
```

Records are provided rather than using `float[]`.

This library breaks from Java naming conventions to use capitalization that matches the color space names, making the code clearer and more aligned with color science literature.

#### Standard Color Models
- **HSV** (Hue, Saturation, Value)
  ```java
  HSV hsv = HSV(rgb);
  RGB rgb = RGB(hsv);
  ```

- **HSL** (Hue, Saturation, Lightness)
  ```java
  HSL hsl = HSL(rgb);
  RGB rgb = RGB(hsl);
  ```

- **HSI** (Hue, Saturation, Intensity)
  ```java
  HSI hsi = HSI(rgb);
  RGB rgb = RGB(hsi);
  ```

#### CIE Color Spaces
- **Lab** (CIELAB)
  ```java
  Lab lab = Lab(rgb);
  RGB rgb = RGB(lab);
  // With white point:
  Lab lab = Lab(rgb, Illuminant.CIE2.D50);
  Lab lab = Lab(rgb, customWhitePoint);
  ```

- **Luv** (CIELUV)
  ```java
  Luv luv = Luv(rgb);
  RGB rgb = RGB(luv);
  ```

- **LCh** (Lab-based cylindrical)
  ```java
  LCh lch = LCh(rgb);
  RGB rgb = RGB(lch);
  ```

- **XYZ** (CIE 1931 XYZ)
  ```java
  XYZ xyz = XYZ(rgb);
  RGB rgb = RGB(xyz);
  ```

#### Perceptually Uniform Spaces
- **Oklab** - Modern perceptually uniform color space
  ```java
  Oklab oklab = Oklab(rgb);
  RGB rgb = RGB(oklab);
  ```

- **Oklch** - Cylindrical version of Oklab
  ```java
  Oklch oklch = Oklch(rgb);
  RGB rgb = RGB(oklch);
  ```

- **Okhsv/Okhsl** - Oklab-based HSV/HSL alternatives
  ```java
  Okhsv okhsv = Okhsv(rgb);
  Okhsl okhsl = Okhsl(rgb);
  ```

#### Video & Broadcasting
- **YCbCr** - Digital video color space
  ```java
  YCbCr ycbcr = YCbCr(rgb, YCbCrColorSpace.ITU_BT_709_HDTV);
  RGB rgb = RGB(ycbcr, YCbCrColorSpace.ITU_BT_709_HDTV);
  ```

#### Printing & Other Spaces
- **CMYK** - Cyan, Magenta, Yellow, Key (Black)
  ```java
  CMYK cmyk = CMYK(rgb);
  RGB rgb = RGB(cmyk);
  ```

### Chromaticity Coordinates
- **xy** (CIE 1931), **uv** (CIE 1960), **u'v'** (CIE 1976)
  ```java
  xy chromaticity = xy(rgb, gamut);
  uv1960 uv1960 = uv1960(chromaticity);
  RGB rgb = RGB(chromaticity, gamut);
  uv uv1976 = uv(rgb);
  ```

## Color Temperature & Lighting

### Correlated Color Temperature (CCT)
```java
// Create RGB from color temperature
RGB warmWhite = RGB(2700, 0.0f);   // 2700K, Duv=0
RGB daylight = RGB(6500, 0.003f);  // 6500K, Duv=0.003

// Calculate CCT from color
float temperature = CCT(rgb);

// Duv - Distance from Planckian locus
float duv = Duv(chromaticity);
```

### RGB + White LED Control
```java
// RGBW (single white channel)
RGBW rgbw = RGBW(rgb, whitePoint);

// RGBWW (dual white channels)
RGBWW rgbww = RGBWW(rgb, warmWhite, coolWhite);

// Create from color temperature (higher potential brightness)
RGBW rgbw = RGBW(3000, 0.8f, whitePoint);  // 3000K at 80% brightness
RGBWW rgbw = RGBWW(3000, 0.8f, warmWhite, coolWhite);
```

## Color Analysis & Utilities

### Color Difference
```java
// Delta E 2000 - Industry standard color difference
float deltaE = deltaE2000(rgb1, rgb2);

// MacAdam steps - Perceptual color difference
float steps = MacAdamSteps(xy1, xy2);
```

### Accessibility & Contrast
```java
// WCAG contrast ratio
float ratio = contrastRatio(foreground, background);

// Check WCAG compliance
boolean meetsAA = WCAG_AA(fg, bg, largeText);
boolean meetsAAA = WCAG_AAA(fg, bg, largeText);
```

### Color Manipulation
```java
// Convert to grayscale
float gray = grayscale(rgb);
```

### Color Harmonies
```java
RGB complementary = complementary(baseColor);
RGB[] triadic = triadic(baseColor);
RGB[] analogous = analogous(baseColor, 30.0f);  // 30° angle
RGB[] splitComp = splitComplementary(baseColor);
```

### Interpolation
```java
// Perceptually uniform interpolation in Oklab space
Oklab blended = lerp(oklab1, oklab2, 0.6f);  // 60% blend
```

## Gamut Management

The `Gamut` class manages color space boundaries:

### Predefined Gamuts
```java
Gamut srgb = Gamut.sRGB;          // Standard RGB
Gamut p3 = Gamut.DisplayP3;       // Display P3
Gamut rec2020 = Gamut.Rec2020;    // Rec. 2020
Gamut full = Gamut.all;           // Full visible spectrum
Gamut huaA = Gamut.PhilipsHue.A;  // Philips Hue
var custom = new Gamut(red, green blue);
```

### Gamut Operations
```java
boolean inGamut = gamut.contains(chromaticity);

xy clamped = gamut.clamp(chromaticity);
```

## Utility Functions

### Gamma Correction
- `sRGB(float linear)` - Apply sRGB gamma encoding
- `linear(float srgb)` - Remove sRGB gamma encoding
- `gammaEncode(float linear, float gamma)` - Custom gamma encoding
- `gammaDecode(float encoded, float gamma)` - Custom gamma decoding

```java
// sRGB gamma encoding/decoding
float encoded = sRGB(linearValue);
float linear = linear(sRGBValue);

// Custom gamma
float encoded = gammaEncode(linear, 2.2f);
float decoded = gammaDecode(encoded, 2.2f);
```

### Data Conversion
- `array(Record record)` - Convert color record to float array

```java
// Convert any color record to float array
float[] rgbArray = array(rgb);  // [r, g, b]
float[] hsvArray = array(hsv);  // [h, s, v]
float[] labArray = array(lab);  // [L, a, b]
```

### Output Formatting
- `hex(float... values)` - Convert to hex color string
- `toString255(float... values)` - Convert to RGB string (0-255 format)
- `dmx8(float value)` - Convert to 8-bit DMX value (0-255)
- `dmx16(float value)` - Convert to 16-bit DMX value (0-65535)

```java
// Hex color string
String hex1 = new RGB(r, g, b).hex();  // "7F7F7F"
String hex2 = hex(r, g, b);            // "7F7F7F"

// RGB string (0-255 format)
String str1 = new RGB(r, g, b).toString255();  // "7F7F7F"
String str2 = toString255(r, g, b);            // "127, 127, 127"

// DMX control
int dmx8bit = dmx8(0.5f);    // 127
int dmx16bit = dmx16(0.5f);  // 32767
```

## Standard Illuminants

The library includes CIE standard illuminants for both 2° and 10° observers:

```java
// 2° observer
XYZ d65_2deg = CIE2.D65;
XYZ d50_2deg = CIE2.D50;

// 10° observer
XYZ d65_10deg = CIE10.D65;
```

Available illuminants: A, C, D50, D55, D65, D75, F2, F7, F11

## Chromatic Adaptation Transforms

```java
// Different CAT methods
LMS lms = LMS(rgb, CAT.Bradford);
LMS lms = LMS(rgb, CAT.VonKries);
LMS lms = LMS(rgb, CAT.HPE);
```

## Usage Examples

### Basic Color Conversion
```java
// Convert RGB to HSV
RGB rgb = new RGB(0.5f, 0.7f, 0.3f);
HSV hsv = HSV(rgb);
System.out.println("Hue: " + hsv.h + "°");
// Convert back to RGB
RGB rgb2 = RGB(hsv);
```

### Perceptual Colors
```java
// Use Oklab for perceptually uniform operations
Oklab color1 = Oklab(rgb1);
Oklab color2 = Oklab(rgb2);
// Interpolate in perceptual space
Oklab middle = lerp(color1, color2, 0.5f);
RGB result = RGB(middle);
```

### Color Temperature to RGB
```java
// Create warm white (2700K)
RGB warmWhite = RGB(2700, 0.0f);
// Create daylight (6500K) with slight green tint
RGB daylight = RGB(6500, -0.003f);
```

### Accessibility Checking
```java
RGB background = new RGB(1, 1, 1);  // White
RGB text = new RGB(0.2f, 0.2f, 0.2f);
float contrast = contrastRatio(text, background);
boolean accessible = WCAG_AA(text, background, false);
if (!accessible) System.out.println("Text color fails WCAG AA standards");
```
