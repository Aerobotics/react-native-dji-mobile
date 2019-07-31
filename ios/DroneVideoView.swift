//
//  DroneVideoView.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/14.
//

import Foundation
import DJISDK

class DroneVideoView: UIView, RCTInvalidating {
  
  var videoFeedView: UIView!
  var videoPreviewer: DJIVideoPreviewer!
  
  public override init(frame: CGRect) {
    super.init(frame: frame)
    //    let screenSize: CGRect = UIScreen.main.bounds
    
    self.videoFeedView = UIView()
    self.addSubview(self.videoFeedView)
    
    self.videoFeedView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    
    self.setupVideoPreviewer()
  }
  
  
  public required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  public func invalidate() {
    resetVideoPreviewer()
  }
  
  func setupVideoPreviewer() {
    self.resetVideoPreviewer()
    //    DJIVideoPreviewer.instance()?.enableHardwareDecode = true
    DJIVideoPreviewer.instance()?.setView(self.videoFeedView)
    DJISDKManager.videoFeeder()?.primaryVideoFeed.add(self, with: nil)
    DJIVideoPreviewer.instance()?.start()
  }
  
  func resetVideoPreviewer() {
    DJIVideoPreviewer.instance()?.unSetView()
    DJISDKManager.videoFeeder()?.primaryVideoFeed.removeAllListeners()
  }
  
}

extension DroneVideoView: DJIVideoFeedListener {
  func videoFeed(_ videoFeed: DJIVideoFeed, didUpdateVideoData videoData: Data) {
    //    rawData.withUnsafeBytes { (ptr: UnsafePointer<UInt8>) in
    //      let p = UnsafeMutablePointer<UInt8>.init(mutating: ptr)
    //      DJIVideoPreviewer.instance()?.push(p, length: Int32(rawData.count))
    //
    //    }
    
    videoData.withUnsafeBytes { (ptr: UnsafePointer<UInt8>) in
      let p = UnsafeMutablePointer<UInt8>.init(mutating: ptr)
      DJIVideoPreviewer.instance()?.push(p, length: Int32(videoData.count))
    }
    
    //    let videoData = rawData as NSData
    //    let videoBuffer = UnsafeMutablePointer<UInt8>.allocate(capacity: videoData.length)
    //    videoData.getBytes(videoBuffer, length: videoData.length)
    //    DJIVideoPreviewer.instance()?.push(videoBuffer, length: Int32(videoData.length))
  }
  
  
}
