MAVEN ?= mvn
DOCKER ?= docker
IMAGE ?= wtc-workhub:local
CONTAINER ?= wtc-workhub
PORT ?= 8080
DATA_DIR ?= $(CURDIR)/.docker-data
STORAGE_PROVIDER ?= local
S3_BUCKET ?=
AWS_REGION ?= af-south-1
S3_PRESIGN_MINUTES ?= 10

.PHONY: build test verify clean run package docker-build docker-run docker-stop

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

docker-build:
	$(DOCKER) build --tag $(IMAGE) .

docker-run:
	mkdir -p $(DATA_DIR)
	$(DOCKER) run --detach --rm \
		--name $(CONTAINER) \
		--user "$$(id -u):$$(id -g)" \
		--publish $(PORT):8080 \
		--volume $(DATA_DIR):/data \
		--env WTC_DB_PATH=/data/workhub.db \
		--env WTC_STORAGE_PROVIDER=$(STORAGE_PROVIDER) \
		--env WTC_S3_BUCKET=$(S3_BUCKET) \
		--env AWS_REGION=$(AWS_REGION) \
		--env WTC_S3_PRESIGN_MINUTES=$(S3_PRESIGN_MINUTES) \
		--env WTC_SEED_DATA=$${WTC_SEED_DATA:-false} \
		$(IMAGE)

docker-stop:
	-$(DOCKER) stop $(CONTAINER)
