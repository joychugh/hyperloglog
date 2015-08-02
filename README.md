[![Build Status](https://travis-ci.org/joychugh/hyperloglog.svg?branch=master)](https://travis-ci.org/joychugh/hyperloglog)

# hyperloglog
hyperloglog implementation in Java 8

This implementation uses standard 8 bit bytes for register size for simplicity of implementation. The error rate should not exceed 2% for cardinalities approaching 10^9

Original Paper: http://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf
