//
//  DJIMedia.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/07/25.
//  Copyright © 2019 Aerobotics. All rights reserved.
//

import Foundation
import DJISDK

@objc(DJIMedia)
class DJIMedia: NSObject {

  @objc(startFullResMediaFileDownload:newFileName:resolve:reject:)
  func startFullResMediaFileDownload(nameOfFileToDownload: String, newFileName: String?, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {

    guard let camera = (DJISDKManager.product() as? DJIAircraft)?.camera else {
      reject("startFullResMediaFileDownload Error", "Camera could not be accessed", nil);
      return
    }

    camera.setMode(.mediaDownload) { (error: Error?) in
      if (error == nil) {
        let mediaManager = camera.mediaManager
        let fileListState = mediaManager?.sdCardFileListState

        if ( (fileListState == DJIMediaFileListState.upToDate) || (fileListState == DJIMediaFileListState.incomplete) || (fileListState == DJIMediaFileListState.reset) ) {
          mediaManager?.refreshFileList(of: .sdCard, withCompletion: { (error: Error?) in
            if (error == nil) {
              let mediaFiles = mediaManager?.sdCardFileListSnapshot()
              if let mediaFile = mediaFiles?.first(where: { $0.fileName == nameOfFileToDownload }) {

                let mediaFileSizeInBytes = mediaFile.fileSizeInBytes
                let eventSendFrequency = EventSender.getEventSendFrequency()
                var completedDownloadInBytes = 0
                var completedPercent = 0.0
                var downloadRate = 0.0

                var fileData = Data()
                let downloadQueue = DispatchQueue(label: "mediaDownloadQueue");
                mediaFile.fetchData(withOffset: 0, update: downloadQueue, update: { (data: Data?, isComplete: Bool, error: Error?) in
                  if let error = error {
                    reject("startFullResMediaFileDownload Error", nil, error);

                  } else {
                    if let data = data {
                      fileData.append(data)

//                      downloadRate = Double(data.count) / eventSendFrequency
//                      completedDownloadInBytes += data.count
//                      completedPercent = Double(completedDownloadInBytes) / Double(mediaFileSizeInBytes)
//                      EventSender.sendReactEvent(type: "mediaFileDownloadEvent", value: [
//                        "eventName": "onProgress",
//                        "percent": completedPercent,
//                        ])
//                                            EventSender.sendReactEvent(type: "mediaFileDownloadEvent", value: [
//                                              "eventName": "onRateUpdate",
//                                              "persize": downloadRate,
//                                              ])
                    }

                    if isComplete {
                      let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
                      let fileUrl = documentsDirectory.appendingPathComponent(newFileName ?? nameOfFileToDownload)
                      do {
                        try fileData.write(to: fileUrl)
                        EventSender.sendReactEvent(type: "mediaFileDownloadEvent", value: [
                          "eventName": "onSuccess",
                        ])
                        resolve(nil)
                      } catch {
                        reject("startFullResMediaFileDownload Error", nil, error);
                      }

                    }
                  }

                })

              } else {
                reject("startFullResMediaFileDownload Error", "Could not find file", nil);
              }


            } else {
              reject("startFullResMediaFileDownload Error", "Could not get file list", nil);
            }
          })

        } else {
          reject("startFullResMediaFileDownload Error", "Could not get file list", nil);
        }

      } else {
        reject("startFullResMediaFileDownload Error", nil, error);
      }
    }

  }

  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }

}
