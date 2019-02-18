//
//  DroneVideoView.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/14.
//

import Foundation
import DJISDK

class DroneVideoView: UIView {
  
  var videoFeedView: UIView!
  var videoPreviewer: DJIVideoPreviewer!
  
  public override init(frame: CGRect) {
    super.init(frame: frame)
    let screenSize: CGRect = UIScreen.main.bounds
    
    self.videoFeedView = UIView()
    self.addSubview(self.videoFeedView)
    
    self.videoFeedView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    
    self.setupVideoPreviewer()
  }
  
  public required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  // TODO: (Adam) call this on product connect
  func setupVideoPreviewer() {
    self.resetVideoPreviewer()
    DJIVideoPreviewer.instance()?.setView(self.videoFeedView)
    DJISDKManager.videoFeeder()?.primaryVideoFeed.add(self, with: nil)
    DJIVideoPreviewer.instance()?.start()
  }
  
  // TODO: (Adam) call this on product disconnect
  func resetVideoPreviewer() {
    DJIVideoPreviewer.instance()?.unSetView()
    DJIVideoPreviewer.instance()?.enableHardwareDecode = true
    //    DJISDKManager.videoFeeder()?.primaryVideoFeed.remove(self)
    DJISDKManager.videoFeeder()?.primaryVideoFeed.removeAllListeners()
  }
  
}

extension DroneVideoView: DJIVideoFeedListener {
  func videoFeed(_ videoFeed: DJIVideoFeed, didUpdateVideoData rawData: Data) {
    let videoData = rawData as NSData
    let videoBuffer = UnsafeMutablePointer<UInt8>.allocate(capacity: videoData.length)
    videoData.getBytes(videoBuffer, length: videoData.length)
    DJIVideoPreviewer.instance()?.push(videoBuffer, length: Int32(videoData.length))
  }
  
  
}
