package com.schedulingcli.states;

import com.schedulingcli.enums.ScreenCode;
import com.schedulingcli.utils.StateManager;

/*
appointmentId | int(10) AI PK
customerId | int(10)
userId | int(11)
title | varchar(255)
description | text
location | text
contact | text
type | text
url | varchar(255)
start | datetime
end | datetime
createDate | datetime
createdBy | varchar(40)
lastUpdate | timestamp
lastUpdateBy | varchar(40)

customerId | int(10) AI PK
customerName | varchar(45)
addressId | int(10)
active | tinyint(1)
createDate | datetime
createdBy | varchar(40)
lastUpdate | timestamp
lastUpdateBy | varchar(40)
 */

public class UpdateRecordState implements BasicState {
    public static void setup() {}

    public static void run() {
        draw();
        teardown();
    }

    public static void draw() {
        System.out.format("Update Record State not yet implemented.%n");
    }

    public static void teardown() {
        StateManager.setCurrentScreen(ScreenCode.MAIN_VIEW);
    }
}
