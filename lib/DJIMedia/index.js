import {NativeModules} from "react-native";

const {
  DJIMedia,
} = NativeModules;


const DJIMediaControl = {
  downloadMedia: async () => {
    await DJIMedia.downloadMedia();
  },
  getFileList: async () => {
    await DJIMedia.getFileList();
  }
};

export default DJIMediaControl;