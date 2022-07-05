package com.aerobotics.DjiMobile;

import dji.common.product.Model;
import dji.sdk.sdkmanager.DJISDKManager;

public class Helpers {

    public static boolean isMultiStreamPlatform() {
        if (DJISDKManager.getInstance() == null){
            return false;
        }
        Model model = DJISDKManager.getInstance().getProduct().getModel();
        return model != null && (model == Model.INSPIRE_2
                || model == Model.MATRICE_200
                || model == Model.MATRICE_210
                || model == Model.MATRICE_210_RTK
                || model == Model.MATRICE_200_V2
                || model == Model.MATRICE_210_V2
                || model == Model.MATRICE_210_RTK_V2
                || model == Model.MATRICE_300_RTK
                || model == Model.MATRICE_600
                || model == Model.MATRICE_600_PRO
                || model == Model.A3
                || model == Model.N3);
    }
}
