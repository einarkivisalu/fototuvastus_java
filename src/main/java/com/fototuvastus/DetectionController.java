package com.fototuvastus;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@Api(tags = "Detection")
public class DetectionController {

    @Autowired
    Detection detectionService;

    @ApiOperation(value = "String", nickname = "Detect Person")
    @RequestMapping(value = "detect", method = RequestMethod.GET, produces = "application/json")
    public String detectPerson() throws IOException {
        return detectionService.run();
    }

    @ApiOperation(value = "String", nickname = "Detect Multiple Persons")
    @RequestMapping(value = "multidetect", method = RequestMethod.GET, produces = "application/json")
    public String detectMultiple() throws IOException {
        return detectionService.runMultiple();
    }

    @ApiOperation(value = "String", nickname = "Detect Base64")
    @RequestMapping(value = "base64", method = RequestMethod.POST, produces = "application/json")
    public String detectBase64(@RequestParam("image") String image) throws IOException {
        return detectionService.runBase64(image);
    }
}
