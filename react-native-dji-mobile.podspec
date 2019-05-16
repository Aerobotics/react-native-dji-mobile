require 'json'
package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |spec|
  spec.name         = 'react-native-dji-mobile'
  spec.version      = package['version']
  spec.summary      = 'React Native DJI Mobile SDK'

  spec.authors      = { 'Aerobotics' => 'adam@aerobotics.com' }
  spec.homepage     = 'https://github.com/Aerobotics/react-native-dji-mobile#README'
  spec.license      = 'MIT'
  spec.platform     = :ios, '9.0'

  spec.source       = { :git => 'https://github.com/Aerobotics/react-native-dji-mobile.git' }
  spec.source_files  = 'ios/**/*.{swift,h,m}'
  spec.exclude_files = ['**/ReactNativeDJIMobileTests/*']
  spec.swift_version = '5.0'

  spec.dependency 'React'
  spec.dependency 'DJI-SDK-iOS', '~> 4.10'
  spec.dependency 'DJIWidget', '~> 1.5'
end
