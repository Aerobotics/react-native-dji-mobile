require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-dji-mobile"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.description  = package["description"]
  s.homepage     = "https://github.com/Aerobotics/react-native-dji-mobile"
  s.license      = "MIT"
  # s.license    = { :type => "MIT", :file => "FILE_LICENSE" }
  s.authors      = { "Aerobotics" => "adam@aerobotics.com" }
  s.platforms    = { :ios => "9.0" }
  s.source       = { :git => "https://github.com/Aerobotics/react-native-dji-mobile.git" }

  s.source_files = "ios/**/*.{h,m,swift}"
  s.requires_arc = true
  s.exclude_files = ['**/ReactNativeDJIMobileTests/*']
  s.swift_version = '5.0'

  s.dependency "React"
  s.dependency 'DJI-SDK-iOS', '~> 4.11'
  s.dependency 'DJIWidget', '~> 1.5'
end

