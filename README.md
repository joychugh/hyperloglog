[![Build Status](https://travis-ci.org/joychugh/hyperloglog.svg?branch=master)](https://travis-ci.org/joychugh/hyperloglog)

# hyperloglog
hyperloglog implementation in Java 8

This implementation uses standard `byte` for register size for simplicity of implementation. The error rate should not exceed 2% for cardinality approaching 10^9

[Documentation](http://joychugh.github.io/hyperloglog/)

[Original Paper](http://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf)
