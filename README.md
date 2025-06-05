<img align="left" width="80" height="80"
src=".github/repo_icon.png" alt="App icon">

# NeuBoard [![Crowdin](https://badges.crowdin.net/neuboard/localized.svg)](https://crowdin.neuboard.patrickgold.dev) [![Matrix badge](https://img.shields.io/badge/chat-%23neuboard%3amatrix.org-blue)](https://matrix.to/#/#neuboard:matrix.org) [![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md) [![NeuBoard CI](https://github.com/neuboard/neuboard/actions/workflows/android.yml/badge.svg?event=push)](https://github.com/neuboard/neuboard/actions/workflows/android.yml)

**NeuBoard** is a free and open-source keyboard for Android 7.0+
devices. It aims at being modern, user-friendly and customizable while
fully respecting your privacy. Currently in early-beta state.

<table>
<tr>
<th align="center" width="50%">
<h3>Stable <a href="https://github.com/neuboard/neuboard/releases/latest"><img alt="Latest stable release" src="https://img.shields.io/github/v/release/neuboard/neuboard?sort=semver&display_name=tag&color=28a745"></a></h3>
</th>
<th align="center" width="50%">
<h3>Preview <a href="https://github.com/neuboard/neuboard/releases"><img alt="Latest preview release" src="https://img.shields.io/github/v/release/neuboard/neuboard?include_prereleases&sort=semver&display_name=tag&color=fd7e14"></a></h3>
</th>
</tr>
<tr>
<td valign="top">
<p><i>Major versions only</i><br><br>Updates are more polished, new features are matured and tested through to ensure a stable experience.</p>
</td>
<td valign="top">
<p><i>Major + Alpha/Beta/Rc versions</i><br><br>Updates contain new features that may not be fully matured yet and bugs are more likely to occur. Allows you to give early feedback.</p>
</td>
</tr>
<tr>
<td valign="top">
<p><a href="https://f-droid.org/packages/dev.patrickgold.neuboard"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="64" alt="F-Droid badge"></a></p>
<p>

**Google Play**: Join the [NeuBoard Test Group](https://groups.google.com/g/neuboard-public-alpha-test), then visit the [testing page](https://play.google.com/apps/testing/dev.patrickgold.neuboard). Once joined and installed, updates will be delivered like for any other app. ([Store entry](https://play.google.com/store/apps/details?id=dev.patrickgold.neuboard))

</p>
<p>

**Obtainium**: [Auto-import stable config][obtainium_stable]

</p>
<p>

**Manual**: Download and install the APK from the release page.

</p>
</td>
<td valign="top">
<p><a href="https://apt.izzysoft.de/fdroid/index/apk/dev.patrickgold.neuboard.beta"><img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="64" alt="IzzySoft repo badge"></a></p>
<p>

**Google Play**: Join the [NeuBoard Test Group](https://groups.google.com/g/neuboard-public-alpha-test), then visit the [preview testing page](https://play.google.com/apps/testing/dev.patrickgold.neuboard.beta). Once joined and installed, updates will be delivered like for any other app. ([Store entry](https://play.google.com/store/apps/details?id=dev.patrickgold.neuboard.beta))

</p>
<p>

**Obtainium**: [Auto-import preview config][obtainium_preview]

</p>
<p>

**Manual**: Download and install the APK from the release page.

</p>
</td>
</tr>
</table>

Beginning with v0.6.0 NeuBoard will enter the public beta on Google Play.

## Highlighted features
- Integrated clipboard manager / history
- Advanced theming support and customization
- Integrated extension support (still evolving)
- Emoji keyboard / history / suggestions

> [!IMPORTANT]
> Word suggestions/spell checking are not included in the current releases
> and are a major goal for the v0.5 milestone.

Feature roadmap: See [ROADMAP.md](ROADMAP.md)

## Contributing
Want to contribute to NeuBoard? That's great to hear! There are lots of
different ways to help out, please see the [contribution guidelines](CONTRIBUTING.md) for more info.

## Addons Store
The official [Addons Store](https://beta.addons.neuboard.org) offers the possibility for the community to share and download NeuBoard extensions.
Instructions on how to publish addons can be found [here](https://github.com/neuboard/neuboard/wiki/How-to-publish-on-NeuBoard-Addons).

Many thanks to Ali ([@4H1R](https://github.com/4H1R)) for implementing the store!

> [!NOTE]
> During the initial beta release phase, the Addons Store _will_ only accept theme extensions.
> Later on we plan to add support for language packs and keyboard extensions.

## List of permissions NeuBoard requests
Please refer to this [page](https://github.com/neuboard/neuboard/wiki/List-of-permissions-NeuBoard-requests)
to get more information on this topic.

## Used libraries, components and icons
* [AndroidX libraries](https://github.com/androidx/androidx) by
  [Android Jetpack](https://github.com/androidx)
* [AboutLibraries](https://github.com/mikepenz/AboutLibraries) by
  [mikepenz](https://github.com/mikepenz)
* [Google Material icons](https://github.com/google/material-design-icons) by
  [Google](https://github.com/google)
* [JetPref preference library](https://github.com/patrickgold/jetpref) by
  [patrickgold](https://github.com/patrickgold)
* [KotlinX coroutines library](https://github.com/Kotlin/kotlinx.coroutines) by
  [Kotlin](https://github.com/Kotlin)
* [KotlinX serialization library](https://github.com/Kotlin/kotlinx.serialization) by
  [Kotlin](https://github.com/Kotlin)

Many thanks to [Nikolay Anzarov](https://www.behance.net/nikolayanzarov) ([@BloodRaven0](https://github.com/BloodRaven0)) for designing and providing the main app icons to this project!

## License
```
Copyright 2020-2025 The NeuBoard Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

Thanks to [The NeuBoard Contributors](https://github.com/neuboard/neuboard/graphs/contributors) for making this project possible!

<!-- BEGIN SECTION: obtainium_links -->
<!-- auto-generated link templates, do NOT edit by hand -->
<!-- see fastlane/update-readme.sh -->
[obtainium_preview]: https://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22dev.patrickgold.neuboard.beta%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fneuboard%2Fneuboard%22%2C%22author%22%3A%22neuboard%22%2C%22name%22%3A%22NeuBoard%20Preview%22%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22preview%5C%22%7D%22%7D%0A
[obtainium_stable]: https://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22dev.patrickgold.neuboard%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fneuboard%2Fneuboard%22%2C%22author%22%3A%22neuboard%22%2C%22name%22%3A%22NeuBoard%20Stable%22%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Afalse%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22stable%5C%22%7D%22%7D%0A
<!-- END SECTION: obtainium_links -->
