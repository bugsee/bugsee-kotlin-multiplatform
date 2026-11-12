// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_library-protect",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_library-protect",
      type: .none,
      targets: ["_library-protect"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_library-protect",
      dependencies: [
      ]
    )
  ]
)
