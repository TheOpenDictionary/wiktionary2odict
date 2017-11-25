ODict ❤ Wiktionary
==================

Wiktionary2ODict is a small Java CLI for converting Wiktionary dumps to ODict-ready XML files. Currently, the CLI only 
supports English as the language of the dictionary, but the entries can be for any language supported on Wiktionary. For
example, to create a Chinese-English dictionary, run:

```bash
$ /path/to/wiktionary2odict/jar zh
```

By default, the script will run and produce a `en_zh.xml` file, which can then be passed to the 
[ODict compiler](https://github.com/odict/odict). If you wish to change the name of the output file, run the JAR with 
the `-o` option:

```bash
$ /path/to/wiktionary2odict/jar -o chinese.xml zh
```

It is recommended to avoid any issues that you have at least Java 8 or up when you run the JAR.