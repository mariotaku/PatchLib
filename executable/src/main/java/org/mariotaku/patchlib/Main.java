package org.mariotaku.patchlib;

import org.apache.commons.cli.ParseException;

import java.io.IOException;

/**
 * Created by mariotaku on 15/12/1.
 */
public class Main {

    public static void main(String[] args) throws ParseException, IOException {
        final PatchLibApplication application = new PatchLibApplication();
        application.start(args);
    }

}
