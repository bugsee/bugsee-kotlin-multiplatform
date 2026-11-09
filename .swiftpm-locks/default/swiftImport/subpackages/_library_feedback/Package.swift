// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_library_feedback",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_library_feedback",
      type: .none,
      targets: ["_library_feedback"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/bugsee/feedback-spm.git",
      exact: "7.0.1"
    )
  ],
  targets: [
    .target(
      name: "_library_feedback",
      dependencies: [
        .product(
          name: "BugseeFeedback",
          package: "feedback-spm"
        )
      ]
    )
  ]
)
