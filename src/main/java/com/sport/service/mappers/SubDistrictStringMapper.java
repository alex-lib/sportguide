package com.sport.service.mappers;

import com.sport.service.bot.constants.KeyboardConstants;
import com.sport.service.entities.enums.place.SubDistrict;

public class SubDistrictStringMapper {

    public static String subDistrictEnumToSubDistrictString(SubDistrict subDistrict) {
        String subDistrictString;
        switch (subDistrict) {
            case SubDistrict.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM ->
                    subDistrictString = KeyboardConstants.FROM_OSTUZHEVO_RING_TO_CHERNAVSKAAY_DAM;
            case SubDistrict.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE ->
                    subDistrictString = KeyboardConstants.FROM_OSTUZHEVO_RING_TO_RAILWAY_BRIDGE;
            case SubDistrict.ELECTRONIKA -> subDistrictString = KeyboardConstants.ELECTRONIKA;
            case SubDistrict.PROCESSOR -> subDistrictString = KeyboardConstants.PROCESSOR;
            case SubDistrict.OTROZHKA -> subDistrictString = KeyboardConstants.OTROZHKA;
            case SubDistrict.DIMITROVA_STREET -> subDistrictString = KeyboardConstants.DIMITROVA_STREET;
            case SubDistrict.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE ->
                    subDistrictString = KeyboardConstants.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE;
            case SubDistrict.VAI_AND_QUARTER_KRASNYY_OKTYABR ->
                    subDistrictString = KeyboardConstants.VAI_AND_QUARTER_KRASNYY_OKTYABR;
            case SubDistrict.PESCHANKA_AND_OZERKI_AND_SHINNIK_1 ->
                    subDistrictString = KeyboardConstants.PESCHANKA_AND_OZERKI_AND_SHINNIK_1;
            case SubDistrict.STARYY_MASHMET -> subDistrictString = KeyboardConstants.STARYY_MASHMET;
            case SubDistrict.BAM -> subDistrictString = KeyboardConstants.BAM;
            case SubDistrict.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA ->
                    subDistrictString = KeyboardConstants.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA;
            case SubDistrict.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK ->
                    subDistrictString = KeyboardConstants.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK;
            case SubDistrict.PRIDONSKOYY_AND_PODKLETNOE ->
                    subDistrictString = KeyboardConstants.PRIDONSKOYY_AND_PODKLETNOE;
            case SubDistrict.TENNISTYY_AND_ZAYYMISHE -> subDistrictString = KeyboardConstants.TENNISTYY_AND_ZAYYMISHE;
            case SubDistrict.SHILOVO -> subDistrictString = KeyboardConstants.SHILOVO;
            case SubDistrict.REST_OF_CENTRALNYY_DISTRICT ->
                    subDistrictString = KeyboardConstants.REST_OF_CENTRALNYY_DISTRICT;
            case SubDistrict.FROM_VGU_TO_SEVERNYY_BRIDGE ->
                    subDistrictString = KeyboardConstants.FROM_VGU_TO_SEVERNYY_BRIDGE;
            case SubDistrict.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP ->
                    subDistrictString = KeyboardConstants.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP;
            case SubDistrict.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET ->
                    subDistrictString = KeyboardConstants.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET;
            case SubDistrict.PODGORNOE -> subDistrictString = KeyboardConstants.PODGORNOE;
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
            case KeyboardConstants.OTROZHKA -> subDistrict = SubDistrict.OTROZHKA;
            case KeyboardConstants.DIMITROVA_STREET -> subDistrict = SubDistrict.DIMITROVA_STREET;
            case KeyboardConstants.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE ->
                    subDistrict = SubDistrict.FROM_CHERNAVSKYY_BRIDGE_TO_VOGRESOVSKYY_BRIDGE;
            case KeyboardConstants.VAI_AND_QUARTER_KRASNYY_OKTYABR ->
                    subDistrict = SubDistrict.VAI_AND_QUARTER_KRASNYY_OKTYABR;
            case KeyboardConstants.PESCHANKA_AND_OZERKI_AND_SHINNIK_1 ->
                    subDistrict = SubDistrict.PESCHANKA_AND_OZERKI_AND_SHINNIK_1;
            case KeyboardConstants.STARYY_MASHMET -> subDistrict = SubDistrict.STARYY_MASHMET;
            case KeyboardConstants.BAM -> subDistrict = SubDistrict.BAM;
            case KeyboardConstants.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA ->
                    subDistrict = SubDistrict.FROM_NOVYY_BOMBEY_TO_ARMADA_TO_DEPUTATKA;
            case KeyboardConstants.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK ->
                    subDistrict = SubDistrict.PERVOE_MAAY_AND_UGO_ZAPADNYY_AND_ZAPADNYY_POSELOK;
            case KeyboardConstants.PRIDONSKOYY_AND_PODKLETNOE -> subDistrict = SubDistrict.PRIDONSKOYY_AND_PODKLETNOE;
            case KeyboardConstants.TENNISTYY_AND_ZAYYMISHE -> subDistrict = SubDistrict.TENNISTYY_AND_ZAYYMISHE;
            case KeyboardConstants.SHILOVO -> subDistrict = SubDistrict.SHILOVO;
            case KeyboardConstants.REST_OF_CENTRALNYY_DISTRICT -> subDistrict = SubDistrict.REST_OF_CENTRALNYY_DISTRICT;
            case KeyboardConstants.FROM_VGU_TO_SEVERNYY_BRIDGE -> subDistrict = SubDistrict.FROM_VGU_TO_SEVERNYY_BRIDGE;
            case KeyboardConstants.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP ->
                    subDistrict = SubDistrict.FROM_45_DIVISII_STREET_TO_9_YANVARY_STREET_AND_TO_MP;
            case KeyboardConstants.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET ->
                    subDistrict = SubDistrict.QUARTER_IPPODROM_AND_FROM_MP_TO_ROTONDA_AND_TO_URITSKOGO_STREET;
            case KeyboardConstants.PODGORNOE -> subDistrict = SubDistrict.PODGORNOE;
            case KeyboardConstants.SEVERNYY -> subDistrict = SubDistrict.SEVERNYY;
            default -> subDistrict = SubDistrict.ALL_SUBDISTRICTS;
        }
        return subDistrict;
    }
}