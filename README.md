[![Build Status][ci-img]][ci]
[![Coverage Status][coveralls-img]][coveralls]
# LambdaSOAP
Put SOAP to REST with AWS Lambda for Java 8

**LambdaSOAP** is a Java library exemplifying a serverless application facilitating [REST][rest]-ful [SOAP][soap] messaging.

Source can be built into a java archive and used as a generic serverless [AWS Lambda function][aws-lambda].

Complex requests can be made by specifying a public URL to locate an example request.

Primitive responses are interpreted as String values and checks for encoding.

Complex responses can be interpreted and marshalled into objects through extension [over modification.][open-closed]

  [ci-img]: https://api.travis-ci.com/connorvanelswyk/LambdaSOAP.svg?branch=master
  [ci]: https://travis-ci.com/connorvanelswyk/LambdaSOAP
  [coveralls-img]: https://coveralls.io/repos/github/connorvanelswyk/LambdaSOAP/badge.svg?branch=master
  [coveralls]: https://coveralls.io/github/connorvanelswyk/LambdaSOAP?branch=master   
  [open-closed]: https://en.wikipedia.org/wiki/Open–closed_principle
  [aws-lambda]: https://aws.amazon.com/lambda/
  [rest]: https://en.wikipedia.org/wiki/Representational_state_transfer
  [soap]: https://en.wikipedia.org/wiki/SOAP
