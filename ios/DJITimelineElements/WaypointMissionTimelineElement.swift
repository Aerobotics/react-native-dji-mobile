//
//  WaypointMissionTimelineElement.swift
//  ReactNativeDjiMobile
//
//  Created by Adam Rosendorff on 2019/03/06.
//

import Foundation
import DJISDK

public class WaypointMissionTimelineElement: DJIMutableWaypointMission {
  
  init(_ parameters: NSDictionary) {
    super.init()
    
    self.finishedAction = DJIWaypointMissionFinishedAction.noAction
    
    guard let autoFlightSpeedParameter = parameters["autoFlightSpeed"] as? Float else {
      // TODO: (Adam) Throw an error here!
      return
    }
    
    guard let maxFlightSpeedParameter = parameters["maxFlightSpeed"] as? Float else {
      // TODO: (Adam) Throw an error here!
      return
    }
    
    guard let waypointsParameter = parameters["waypoints"] as? [Any] else {
      // TODO: (Adam) Throw an error here!
      return
    }
    
    
    
    self.autoFlightSpeed = autoFlightSpeedParameter
    self.maxFlightSpeed = maxFlightSpeedParameter
    
    // TODO: (Adam) Validate each waypoint!
    for case let waypointData as [String: Double] in waypointsParameter {
      let waypointCoordinate = CLLocationCoordinate2D.init(latitude: waypointData["latitude"]!, longitude: waypointData["longitude"]!)
      let waypoint = DJIWaypoint.init(coordinate: waypointCoordinate)
      waypoint.altitude = Float(waypointData["altitude"]!)
      self.add(waypoint)
    }
        
  }
}
