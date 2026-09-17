# CI/CD

Three workflows. They replace the gerrit patchset and merge jobs and reuse the
repo's own `scripts/{build,test,deploy}.sh`, so CI and local runs stay identical.

| Workflow | Trigger | Environment | `RELEASE` | What it does |
|---|---|---|---|---|
| `pr.yml` | PR → `main` / `release` | — | `false` | `build.sh` + `test.sh all` |
| `main.yml` | push to `main`, manual | — | `false` | `build.sh` + `test.sh all` + `deploy.sh --local` (mavenLocal) |
| `deploy-production.yml` | manual dispatch from `release` | `production` | `true` | `build.sh` + `deploy.sh` (tests, then publish + auto-release to Maven Central) |

`test.sh all` = Robolectric unit tests and iOS simulator tests for both
`:library` and `:library-protect`. `build.sh` assembles every published target,
including the `iosArm64` device slice that the simulator tests never compile.

## Runners: GitHub-hosted, not the org Mac

`bugsee-android` and `bugsee-cocoa` run on the org's self-hosted Mac
(`[self-hosted, macOS, ARM64]`). This repo deliberately does **not**:

- **It cannot.** That runner's group (`Default`) is restricted to selected
  repositories with *public repositories disallowed*, and this repo is public.
  Jobs targeting it would queue forever.
- **It should not.** Opening that runner to a public repo lets pull requests from
  forks execute code on a persistent machine holding other repos' credentials
  (signing keyring, AWS profile). The fork-PR approval policy only covers
  first-time contributors.

GitHub-hosted runners are free and unmetered for public repositories, and each
job gets a clean VM — so none of the shared-runner workarounds (`--clean`,
disabled file watching, on-disk report archiving) are needed.

The shared `.github/actions/setup` action:

- selects **Xcode 16.4** explicitly (`gradle.properties` carries 16.4
  workarounds) rather than trusting the image default — bump its
  `xcode-version` default deliberately;
- installs Temurin **JDK 21** (AGP 8 needs 17+);
- restores the Gradle cache (`gradle/actions/setup-gradle`, written only from
  `main`) and `~/.konan` (Kotlin/Native toolchain, keyed on
  `gradle/libs.versions.toml`).

The Android SDK and CocoaPods (needed for the `Bugsee` pod cinterop) come with the
`macos-15` image.

Test reports are uploaded as an artifact **only on failure**, kept 7 days —
artifact storage is an org-wide quota shared with every repo.

## Why there is no staging / SNAPSHOT deploy

`bugsee-android` publishes a SNAPSHOT on every push to `main`. This repo cannot:
it publishes through the Sonatype **Central Portal** with
`com.vanniktech.maven.publish` **0.29.0**, which throws *"Snapshots are not
supported when publishing through the central portal."* `deploy.sh` now refuses a
remote SNAPSHOT publish up front instead of failing after the test run.

`main.yml` runs `deploy.sh --local` instead. That still exercises every
publication — POMs, KMP metadata, all Android/iOS targets — so a broken
publishing setup shows up on merge, not on release day.

Real SNAPSHOTs would need plugin ≥ 0.33 (Central Portal snapshot support) plus
snapshots enabled for the `com.bugsee` namespace on central.sonatype.com.

## Releasing

1. On `release`, bump `version.txt` (and SDK versions in
   `gradle/libs.versions.toml` if needed) through a PR, so `pr.yml` runs.
2. Actions → **Deploy (production)** → *Run workflow*, branch **`release`**, type
   the version from `version.txt`.

A Maven Central release is permanent — it cannot be deleted or re-published under
the same version.

The workflow file must exist on `release` for step 2 (dispatch runs the file from
the chosen branch), so keep `release` merged up from `main`.

## Environment and secrets

`production` environment with deployment branch policy **`release` only**. That
policy is the real guard — required reviewers are not available on the current
plan — and it also means the secrets below are never handed to a run from any
other branch or from a fork. Keep the policy in place.

Secrets on the `production` environment:

| Name | Purpose |
|---|---|
| `MAVEN_CENTRAL_USERNAME` | Central Portal **user token** name (not the account login) — same value as `mavenCentralUsername` |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal user token password — same value as `mavenCentralPassword` |
| `SIGNING_KEY` | ASCII-armored PGP **private** key block, header and footer included |
| `SIGNING_PASSWORD` | Its passphrase — same value as `signing.password` |

```
gh secret set MAVEN_CENTRAL_USERNAME --env production --repo bugsee/bugsee-kotlin-multiplatform
gh secret set MAVEN_CENTRAL_PASSWORD --env production --repo bugsee/bugsee-kotlin-multiplatform
gpg --armor --export-secret-keys <signing.keyId> | gh secret set SIGNING_KEY --env production --repo bugsee/bugsee-kotlin-multiplatform
gh secret set SIGNING_PASSWORD       --env production --repo bugsee/bugsee-kotlin-multiplatform
```

**All four are already set.** They carry the same credentials as
`bugsee-android` (same `com.bugsee` namespace, same key):
`MAVEN_CENTRAL_USERNAME`/`PASSWORD` = its `NEXUS_USERNAME`/`PASSWORD` (Central
Portal user token), `SIGNING_KEY`/`SIGNING_PASSWORD` = its secrets of the same
name — master key `0DEF44F1F9AB1FCF` (short id `F9AB1FCF`) plus subkey
`4CFA136C`. Rotate them in both repos together.

Armoring is only an encoding, so the export needs no passphrase. The secrets
reach only the two steps that need them, not the whole job.

### Why signing needed a build change

The classic `signing.secretKeyRingFile` is a filesystem path, which cannot be put
in a secret. The workflow therefore passes
`ORG_GRADLE_PROJECT_signingInMemoryKey` / `…KeyPassword`, which the vanniktech
plugin reads natively. Both `build.gradle.kts` files now enable
`signAllPublications()` when **either** `signing.keyId` or `signingInMemoryKey` is
present, and `deploy.sh` accepts either set — local releases with
`~/.gradle/gradle.properties` work exactly as before.
