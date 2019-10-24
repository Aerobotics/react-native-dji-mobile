//
//  FolderMonitor.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/08/08.
//  Copyright © 2019 Facebook. All rights reserved.
//

import Foundation

class FolderMonitor {
  
  var source: DispatchSourceFileSystemObject?
  var pathToMonitor: String
  var filesSnapshot: [URL] = []
  
  init() {
    self.pathToMonitor = ""
  }
  
  /**
   * Note: This currently only monitors for new files, nothing else!
   */
  func startMonitoring(pathToMonitor: String, callback: @escaping (String) -> Void) {
    self.pathToMonitor = pathToMonitor
    self.stopMonitoring()
    let fileDescriptor = open(pathToMonitor, O_EVTONLY)
    if (fileDescriptor == -1) {
      return
    }
    
    let pathURL = URL(fileURLWithPath: pathToMonitor)
    do {
      self.filesSnapshot = try FileManager.default.contentsOfDirectory(at: pathURL, includingPropertiesForKeys: nil)
    } catch {
      print("Error while enumerating files \(pathURL.path): \(error.localizedDescription)")
    }
    
    self.source = DispatchSource.makeFileSystemObjectSource(fileDescriptor: fileDescriptor, eventMask: .write, queue: DispatchQueue.global())
    self.source!.setEventHandler {
      
      do {
        let newFilesSnapshot = try FileManager.default.contentsOfDirectory(at: pathURL, includingPropertiesForKeys: nil)
        for fileUrl in newFilesSnapshot {
          if !self.filesSnapshot.contains(fileUrl) {
            callback(fileUrl.lastPathComponent)
          }
        }
        self.filesSnapshot = newFilesSnapshot
      } catch {
        print("Error while enumerating files \(pathURL.path): \(error.localizedDescription)")
      }
      
    }
    self.source!.resume()
  }
  
  func stopMonitoring() {
    source?.cancel()
    source = nil
  }
  
}
