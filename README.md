# lein-license

__lein-license__ is a [Leiningen][leiningen] plugin for project-level license
management, i.e. mainly for setting or updating a project's license information
in:

- the `LICENSE` file,
- the `README`,
- as well as the `project.clj`.

__Use cases__ thus include:

- setting the initial license for your Leiningen project if the EPL does not fit
  your needs,
- automatically updating e.g. authors, emails and copyright notices in your
  license files,
- as well as completely and seamlessly switching between licenses.

It utilises the data provided by [choosealicense.com][choosealicense] to
generate the license text. Make sure to visit that page if you're unsure (or
never thought of) which license suits your project best!

[leiningen]: https://github.com/technomancy/leiningen
[choosealicense]: http://choosealicense.com/

## Artifact

__Leiningen__ ([via Clojars](https://clojars.org/lein-license))

Add the following to your `:user` profile's  `:plugins` vector in
`~/.lein/profiles.clj`.

[![Clojars Project](http://clojars.org/lein-license/latest-version.svg)](http://clojars.org/lein-license)

## Usage

### List Licenses

To list all licenses provided by [choosealicense.com][choosealicense] and thus
this plugin, run:

```
lein license list
```

You can also filter using a prefix, e.g.:

```
lein license list gpl
```

The resulting output will look like the following:

```
  2 License(s) found:

      gpl-2.0          (http://choosealicense.com/licenses/gpl-2.0)
      gpl-3.0          (http://choosealicense.com/licenses/gpl-3.0)

  Visit http://choosealicense.com for a comprehensive overview
  and comparison of these licenses.
```

Note that the license list is cached in `$LEIN_HOME/.licenses.edn` to prevent
hitting of the rate limit set by Github's API (which is used to retrieve the
license list from the repository `github/choosealicense.com`).

### Set &amp; Update a License

After you've decided on which license to use, backup your changes (you never
know) and pass the license identifier to the `update` task, e.g.:

```
lein license update mit
```

(Have a look at `lein help license update` for the available options.)

If you've already run the above command once, you can update the license text
(e.g. with the current year, new author, ...) by simply issuing:

```
lein license update
```

The output will indicate progress:

```
License: MIT License
* updating project.clj ...
* updating LICENSE ...
* updating README ...
License updated.
```

For `README` updates, note the following:

- only `README.md` and `README.markdown` are currently supported,
- if it contains a block starting with `## License` it will be updated.
- short licenses will be inlined into the `README`, longer ones will be linked
  to by a copyright notice.

In the `project.clj` a `:license` map will either be inserted or updated. Note
that a `:key` value will be added for lein-license to be able to identify the
current license.

### Preview/Render a License

To see what a license would look like using the currently available information,
pass its identifier to:

```
lein license render <identifier>
```

This will print the license text to your console.

## License Data

You can add some additional information to the `:license` map in your
`project.clj` which will be preserved in updates and used for rendering the
license:

```clojure
  :license {:author "Steve"
            :email  "steve@steve.steve"
            :year   2013}
```

`:author` and `:email` will be used directly in the license text, while `:year`
(if given) will cause the copyright notice to contain e.g. `2013-2015` instead
of just the current one.

You can, of course, also set this information in your global `:user` profile.

## Git Integration

If you're using Git and the configuration values `user.name` and `user.email`
are set correctly they will be used as default values for the license author and
email address.

## License

```
The MIT License (MIT)

Copyright (c) 2015 Yannick Scherer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
