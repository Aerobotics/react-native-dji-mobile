# Media Control

## DJIMediaManager

DJIMediaManager is used for downloading images & videos from the drone to the app over the remote connection.

## Functions:

#### `startFullResMediaFileDownload(nameOfFileToDownload: string, newFileName: ?string): Promise`
**N.B.** Only one instance of this function should be called at any time. Calling multiple instances may lead to unexpected results
and possible app crashes.
