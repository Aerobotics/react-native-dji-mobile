import {
  NativeModules,
} from 'react-native';
import {
  DJIEventSubject,
} from '../utilities';
import {
  filter as $filter,
} from 'rxjs/operators';

const {
  DJIMedia,
} = NativeModules;


const DJIMediaControl = {
  startFullResMediaFileDownload:  async (fileName: string, newFileName?: string) => {
    await DJIMedia.startFullResMediaFileDownload(fileName, newFileName);
    return DJIEventSubject.pipe(
      $filter(evt => evt.type === 'mediaFileDownloadEvent'),
    );
  },
  startFullResMediaFileDownloadListener: async () => {
    return DJIEventSubject.pipe(
      $filter(evt => evt.type === 'mediaFileDownloadEvent'),
    );
  },
};

export default DJIMediaControl;
