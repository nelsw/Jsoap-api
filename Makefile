.PHONY: compile build ship it

it: ship

compile:
	mvn clean install

build: compile
	sam local generate-event apigateway aws-proxy \
    	--debug \
    	--method POST \
    	--stage LATEST \
    	--body '{ \
    	"wsdl": "https://graphical.weather.gov:443/xml/SOAP_server/ndfdXMLserver.php", \
    	"xml": "https://graphical.weather.gov/xml/docs/SOAP_Requests/LatLonListZipCode.xml" \
    	}' \
    	| sam local invoke jsoap

ship: build
	aws lambda update-function-code \
    --function-name jsoap \
    --zip-file fileb://./target/jsoap-api-1.0-SNAPSHOT.jar