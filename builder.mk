#
# makefile used on the builder at shanoir-2016.irisa.fr
#

# designed to build all images from scratch

#TODO: enable studies & studycards
MICROSERVICES = users #studies studycards

# keycloak images
#
# https://github.com/fli-iam/shanoir-ng/wiki/Installation-guide-3%29-Docker-Keycloak
keycloak:
	(cd shanoir-ng-keycloak-init    && mvn package)
	(cd shanoir-ng-keycloak-auth	&& mvn package)
	cp shanoir-ng-keycloak-auth/target/shanoir-ng-keycloak-auth-0.0.1-SNAPSHOT.jar	\
	   shanoir-ng-keycloak/docker/shanoir-ng-keycloak-auth.jar 
	(cd shanoir-ng-keycloak/docker/ && docker build -t shanoir-ng/keycloak .)

users: keycloak-package
keycloak-package:
	# prepare the package for the shanoir-ng-users package
	(cd shanoir-ng-users/ && mvn install -Pinit-keycloak)
	(cd shanoir-ng-keycloak && mvn package)

# base image
base-image:
	docker build -t shanoir-ng/base shanoir-ng-template/shanoir-ng-base

# base image for the microservices
base-ms-image: base-image
	docker build -t shanoir-ng/base-ms shanoir-ng-template/shanoir-ng-base-ms

# microservices
$(MICROSERVICES): %: base-ms-image
	# https://github.com/fli-iam/shanoir-ng/wiki/Installation-guide-4%29-Docker-shanoir-ng-users
	(cd 'shanoir-ng-$@/' && mvn clean package docker:build -DskipTests -Pqualif)

# shanoir-ng-nginx
#
# https://github.com/fli-iam/shanoir-ng/wiki/Installation-guide-6%29-Docker-Nginx-with-staticsa
#nginx: keycloak users studies studycards
nginx: base-ms-image $(MICROSERVICES)
	npm set registry https://registry.npmjs.org

	(cd shanoir-ng-front && mvn package -Pqualif)
	(cd shanoir-ng-nginx && docker build -t shanoir-ng/nginx:latest .)

# all images for shanoir NG
all: keycloak $(MICROSERVICES) nginx
