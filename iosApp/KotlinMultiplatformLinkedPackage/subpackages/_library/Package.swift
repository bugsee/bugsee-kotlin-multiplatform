// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_library",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_library",
      type: .none,
      targets: ["_library"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/bugsee/spm.git",
      exact: "7.0.1"
    )
  ],
  targets: [
    .target(
      name: "_library",
      dependencies: [
        .product(
          name: "Bugsee",
          package: "spm"
        )
      ]
    )
  ]
)
