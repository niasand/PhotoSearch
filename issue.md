
## [2026-09-25] setup-android v3 CI 全挂：Google 下架 legacy tools 包

- **现象**：push 到 main 后 CI 在 `Setup Android SDK` 步骤必挂，报 `Warning: Failed to find package 'tools'` → `sdkmanager exit code 1`；三个 Android 仓库同一天集体复发。
- **根因**：`android-actions/setup-android@v3` 内部会安装已被 Google 从 SDK 仓库移除的 legacy `tools` 包；runner 镜像更新到 cmdline-tools 16.0 后该包彻底不存在。
- **修复方案**：统一升级 `android-actions/setup-android@v3` → `@v4`（v4.0.4，2026-09-17 发布）；PhotoSearch 显式 `sdkmanager "tools"` 一并移除。
- **涉及文件**：`.github/workflows/build-release.yml` / `.github/workflows/android-ci.yml` / `.github/workflows/release.yml`
- **验证证据**：commit `Bump setup-android to v4 to fix missing tools package` 后 CI 通过（见 Actions run）。
- **教训**：第三方 action 钉大版本（@v3）不等于稳定；SDK 供应商下架包会让老 action 整体挂掉。Android CI 至少每季度空跑一次 main，挂了先看 action 上游 release 而不是先怀疑自己的代码。

## [2026-09-25] PhotoSearch release 从未产出 APK：lintVitalRelease 判 backup_rules 冗余 exclude 为 fatal

- **现象**：CI 到 `assembleRelease` 必挂（三月至今），两个历史 Release 全是空资产；报错 `FullBackupContent: photo_search_db is not in an included path`。
- **根因**：`fullBackupContent` 语义是白名单——只 include 了 sharedpref，database 域本就不会被备份，`<exclude domain="database" ...>` 是冗余规则，lintVital 直接 fatal。上一条 issue 里"CI 通过"的结论只对 heartbeat/AltimeterApp 成立，PhotoSearch 修完 setup-android 后又暴露了这层。
- **修复方案**：删除 `backup_rules.xml` 和 `data_extraction_rules.xml` 里冗余的 exclude 行；include-only sharedpref 本身就实现了"备 prefs、不备 DB"的原始意图。
- **涉及文件**：`app/src/main/res/xml/backup_rules.xml`、`app/src/main/res/xml/data_extraction_rules.xml`
- **验证证据**：修复 commit 后 Actions `assembleRelease` 通过，Release 页产出带 APK 资产的包（见 Actions run）。
- **教训**：fullBackupContent/dataExtractionRules 的 exclude 只能写在 include 范围内，"没 include 的域 + exclude" 不是保险而是 fatal lint；写备份规则先确认语义是白名单还是黑名单。
