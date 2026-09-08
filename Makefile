MAVEN ?= mvn

.PHONY: build test verify clean run package

build:
	$(MAVEN) compile

test:
	$(MAVEN) test

verify:
	$(MAVEN) verify

clean:
	$(MAVEN) clean

run:
	$(MAVEN) spring-boot:run

package:
	$(MAVEN) package
