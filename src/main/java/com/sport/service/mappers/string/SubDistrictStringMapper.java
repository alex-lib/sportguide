package com.sport.service.mappers.string;

import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.entities.enums.place.SubDistrict;

public class SubDistrictStringMapper {

    public static String subDistrictEnumToSubDistrictString(SubDistrict subDistrict) {
        if (subDistrict == null) {
            return "";
        }
        String subDistrictString;
        switch (subDistrict) {
            case SubDistrict.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM ->
                    subDistrictString = KeyboardConstants.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM;
            case SubDistrict.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE ->
                    subDistrictString = KeyboardConstants.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE;
            case SubDistrict.ELECTRONIKA -> subDistrictString = KeyboardConstants.ELECTRONIKA;
            case SubDistrict.PROCESSOR -> subDistrictString = KeyboardConstants.PROCESSOR;
            case SubDistrict.OTROZHKA_BOROVOE_SOMOVO -> subDistrictString = KeyboardConstants.OTROZHKA_BOROVOE_SOMOVO;
            case SubDistrict.DIMITROVA_STREET -> subDistrictString = KeyboardConstants.DIMITROVA_STREET;
            case SubDistrict.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE ->
                    subDistrictString = KeyboardConstants.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE;
            case SubDistrict.VAI_KRASNYY_OKTYABR -> subDistrictString = KeyboardConstants.VAI_KRASNYY_OKTYABR;
            case SubDistrict.PESCHANKA_OZERKI_SHINNIK_1_STARYY_MASHMET_BAM_MASHMET ->
                    subDistrictString = KeyboardConstants.PESCHANKA_OZERKI_SHINNIK_1_STARYY_MASHMET_BAM_MASHMET;
            case SubDistrict.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA ->
                    subDistrictString = KeyboardConstants.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA;
            case SubDistrict.PERVOE_MAAY_UGO_ZAPADNYY_ZAPADNYY_POSELOK ->
                    subDistrictString = KeyboardConstants.PERVOE_MAAY_UGO_ZAPADNYY_ZAPADNYY_POSELOK;
            case SubDistrict.PRIDONSKOYY_PODKLETNOE -> subDistrictString = KeyboardConstants.PRIDONSKOYY_PODKLETNOE;
            case SubDistrict.TENNISTYY_ZAYYMISHE -> subDistrictString = KeyboardConstants.TENNISTYY_ZAYYMISHE;
            case SubDistrict.SHILOVO -> subDistrictString = KeyboardConstants.SHILOVO;
            case SubDistrict.REST_OF_CENTRALNYY_DISTRICT ->
                    subDistrictString = KeyboardConstants.REST_OF_CENTRALNYY_DISTRICT;
            case SubDistrict.FROM_VGU_TO_SEVERNYY_BRIDGE ->
                    subDistrictString = KeyboardConstants.FROM_VGU_TO_SEVERNYY_BRIDGE;
            case SubDistrict.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP ->
                    subDistrictString = KeyboardConstants.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP;
            case SubDistrict.IPPODROM_FROM_MP_TO_ROTONDA_TO_URITSKOGO_STREET ->
                    subDistrictString = KeyboardConstants.IPPODROM_FROM_MP_TO_ROTONDA_TO_URITSKOGO_STREET;
            case SubDistrict.PODGORNOE_HVOINYY_ZADONIE ->
                    subDistrictString = KeyboardConstants.PODGORNOE_HVOINYY_ZADONIE;
            case SubDistrict.SEVERNYY -> subDistrictString = KeyboardConstants.SEVERNYY;
            default -> subDistrictString = KeyboardConstants.ALL_SUBDISTRICTS;
        }
        return subDistrictString;
    }

    public static SubDistrict subDistrictStringToSubDistrictEnum(String subDistrictString) {
        SubDistrict subDistrict;
        switch (subDistrictString) {
            case KeyboardConstants.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM ->
                    subDistrict = SubDistrict.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM;
            case KeyboardConstants.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE ->
                    subDistrict = SubDistrict.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE;
            case KeyboardConstants.ELECTRONIKA -> subDistrict = SubDistrict.ELECTRONIKA;
            case KeyboardConstants.PROCESSOR -> subDistrict = SubDistrict.PROCESSOR;
            case KeyboardConstants.OTROZHKA_BOROVOE_SOMOVO -> subDistrict = SubDistrict.OTROZHKA_BOROVOE_SOMOVO;
            case KeyboardConstants.DIMITROVA_STREET -> subDistrict = SubDistrict.DIMITROVA_STREET;
            case KeyboardConstants.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE ->
                    subDistrict = SubDistrict.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE;
            case KeyboardConstants.VAI_KRASNYY_OKTYABR -> subDistrict = SubDistrict.VAI_KRASNYY_OKTYABR;
            case KeyboardConstants.PESCHANKA_OZERKI_SHINNIK_1_STARYY_MASHMET_BAM_MASHMET ->
                    subDistrict = SubDistrict.PESCHANKA_OZERKI_SHINNIK_1_STARYY_MASHMET_BAM_MASHMET;
            case KeyboardConstants.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA ->
                    subDistrict = SubDistrict.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA;
            case KeyboardConstants.PERVOE_MAAY_UGO_ZAPADNYY_ZAPADNYY_POSELOK ->
                    subDistrict = SubDistrict.PERVOE_MAAY_UGO_ZAPADNYY_ZAPADNYY_POSELOK;
            case KeyboardConstants.PRIDONSKOYY_PODKLETNOE -> subDistrict = SubDistrict.PRIDONSKOYY_PODKLETNOE;
            case KeyboardConstants.TENNISTYY_ZAYYMISHE -> subDistrict = SubDistrict.TENNISTYY_ZAYYMISHE;
            case KeyboardConstants.SHILOVO -> subDistrict = SubDistrict.SHILOVO;
            case KeyboardConstants.REST_OF_CENTRALNYY_DISTRICT -> subDistrict = SubDistrict.REST_OF_CENTRALNYY_DISTRICT;
            case KeyboardConstants.FROM_VGU_TO_SEVERNYY_BRIDGE -> subDistrict = SubDistrict.FROM_VGU_TO_SEVERNYY_BRIDGE;
            case KeyboardConstants.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP ->
                    subDistrict = SubDistrict.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP;
            case KeyboardConstants.IPPODROM_FROM_MP_TO_ROTONDA_TO_URITSKOGO_STREET ->
                    subDistrict = SubDistrict.IPPODROM_FROM_MP_TO_ROTONDA_TO_URITSKOGO_STREET;
            case KeyboardConstants.PODGORNOE_HVOINYY_ZADONIE -> subDistrict = SubDistrict.PODGORNOE_HVOINYY_ZADONIE;
            case KeyboardConstants.SEVERNYY -> subDistrict = SubDistrict.SEVERNYY;
            default -> subDistrict = SubDistrict.ALL_SUBDISTRICTS;
        }
        return subDistrict;
    }
}