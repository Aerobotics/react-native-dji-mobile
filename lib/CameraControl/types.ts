export type PhotoFileFormat =
  | 'RAW'
  | 'JPEG'
  | 'RAW_AND_JPEG'
  | 'TIFF_8_BIT'
  | 'TIFF_14_BIT'
  | 'TIFF_14_BIT_LINEAR_LOW_TEMP_RESOLUTION'
  | 'TIFF_14_BIT_LINEAR_HIGH_TEMP_RESOLUTION'
  | 'RADIOMETRIC_JPEG'
  | 'RADIOMETRIC_JPEG_LOW'
  | 'RADIOMETRIC_JPEG_HIGH'
  | 'UNKNOWN';

export type DjiPhotoAspectRatio =
  | 'RATIO_4_3'
  | 'RATIO_16_9'
  | 'RATIO_3_2'
  | 'RATIO_1_1'
  | 'RATIO_18_9'
  | 'RATIO_5_4'
  | 'UNKNOWN'

export type PhotoAspectRatio =
  | '4:3'
  | '16:9'
  | '3:2'
  | '1:1'
  | '18:9'
  | '5:4'
  | 'Unknown';

export type DjiExposureModes =
  | 'PROGRAM'
  | 'SHUTTER_PRIORITY'
  | 'APERTURE_PRIORITY'
  | 'MANUAL'
  | 'CINE'
  | 'UNKNOWN'
