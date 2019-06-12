package com.thalesgroup.datastorage.dojo;

import com.github.javafaker.Faker;
import lombok.experimental.UtilityClass;

@UtilityClass
public class NameGenerator {

    public static String getName() {
        return Faker.instance().name().fullName();
    }
}
