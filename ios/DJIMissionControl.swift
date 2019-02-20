//
//  DJIMissionControl.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/02/20.
//

import Foundation
import DJISDK

@objc(DJIMissionControlWrapper)
class DJIMissionControlWrapper: NSObject {
  
  var missionIdIndex = 0
  var waypointMissions: [Int: DJIWaypointMission] = [:]
  
  @objc(createWaypointMission:resolve:reject:)
  func createWaypointMission(coordinates: NSArray, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let waypointMission = DJIMutableWaypointMission.init()
    var waypoints: [DJIWaypoint] = []
    for case let item as [String: Double] in coordinates {
      let waypointCoordinate = CLLocationCoordinate2D.init(latitude: item["latitude"]!, longitude: item["longitude"]!)
      let waypoint = DJIWaypoint.init(coordinate: waypointCoordinate)
      waypoint.altitude = Float(item["altitude"]!)
      waypoints.append(waypoint)
      waypointMission.addWaypoints(waypoints)
      
      let error = waypointMission.checkParameters()
      if (error != nil) {
        reject("Waypoint mission invalid", (error! as NSError).localizedDescription, nil)
        return
      } else {
        waypointMissions[missionIdIndex] = waypointMission
        resolve(missionIdIndex)
        missionIdIndex += 1
      }
    }
  }
  
  @objc(destroyWaypointMission:resolve:reject:)
  func destroyWaypointMission(missionId: NSNumber, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
    let removedMission = waypointMissions.removeValue(forKey: missionId.intValue)
    // TODO: (Adam) Should this reject if no valid mission was found?
    resolve(nil)
  }
  
  //  @objc(createWaypointMission:resolve:reject:)
  //  func createWaypointMission(coordinates: NSArray, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
  //    let waypointMission = DJIMutableWaypointMission.init()
  //    var waypoints: [DJIWaypoint] = []
  //    for case let item as [String: Double] in coordinates {
  //      let waypointCoordinate = CLLocationCoordinate2D.init(latitude: item["latitude"]!, longitude: item["longitude"]!)
  //      waypoints.append(DJIWaypoint.init(coordinate: waypointCoordinate))
  //      //      let waypoint = DJIWaypoint.init(coordinate: waypointCoordinate)
  //      //      print(item["latitude"])
  //      //      print(item["longitude"])
  //    }
  //    waypointMission.addWaypoints(waypoints)
  //    let error = waypointMission.checkParameters()
  //    if (error != nil) {
  //      reject("Waypoint invalid", (error! as NSError).localizedDescription, nil)
  //      return
  //    }
  //
  //    print("START FLIGHT")
  //    DJISDKManager.missionControl()?.scheduleElement(waypointMission)
  //    DJISDKManager.missionControl()?.startTimeline()
  //
  //    DJISDKManager.missionControl()?.addListener(self, toTimelineProgressWith: { (event: DJIMissionControlTimelineEvent, element: DJIMissionControlTimelineElement?, error: Error?, info: Any?) in
  //      print("MISSION EVENT")
  //      if (error != nil) {
  //        print(error!.localizedDescription)
  //      }
  //      print(event.rawValue)
  //    })
  //
  //    //    DJIMissionControl.scheduleElement(waypointMission as DJIMissionControlTimelineElement)
  //
  //    resolve(nil)
  //  }
  
}
