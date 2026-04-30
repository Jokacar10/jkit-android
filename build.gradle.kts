/*
 * Copyright (c) 2025 TonTech
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

tasks.register("assembleAll") {
    group = "build"
    description = "Build+copy the SDK AAR and assemble the demo APK."
    dependsOn(
        gradle.includedBuild("TONWalletKit-Android").task(":buildAndCopyToDemo"),
        gradle.includedBuild("AndroidDemo").task(":app:assembleDebug"),
    )
}

tasks.register("cleanAll") {
    group = "build"
    description = "Clean both included builds."
    dependsOn(
        gradle.includedBuild("TONWalletKit-Android").task(":clean"),
        gradle.includedBuild("AndroidDemo").task(":clean"),
    )
}

tasks.register("testAll") {
    group = "verification"
    description = "Run SDK unit tests + demo unit tests."
    dependsOn(
        gradle.includedBuild("TONWalletKit-Android").task(":api:testDebugUnitTest"),
        gradle.includedBuild("TONWalletKit-Android").task(":impl:testDebugUnitTest"),
        gradle.includedBuild("AndroidDemo").task(":app:testDebugUnitTest"),
    )
}
