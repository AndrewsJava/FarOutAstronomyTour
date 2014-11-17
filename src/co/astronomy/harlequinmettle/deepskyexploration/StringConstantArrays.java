package co.astronomy.harlequinmettle.deepskyexploration;

public interface StringConstantArrays {
	public static final String APOD_BASE_US = "http://apod.nasa.gov/apod/";
	public static final String APOD_BASE = "http://www.star.ucl.ac.uk/~apod/apod/";
	public static final String WIKI_BASE = "http://en.wikipedia.org/wiki/";
	public static final String[] IMAGE_NAMES_ARRAY = { "aac_8", "aae_400", "aaf_420", "aag_450", "aai_700", "aak_1000", "aal_1140",
			"aam_1500", "aaq_1500", "aar_1500", "aat_1500", "aau_2000", "aaw_2000", "aay_2000", "aaz_2000", "aba_2100", "abb_2300",
			"abh_3000", "abi_3000", "abj_3000", "abk_4000", "abl_4000", "abm_4000", "abn_4000", "abp_5000", "abq_5000", "abr_5000",
			"abt_5500", "abu_5500", "abv_6000", "abw_6500", "abx_6500", "aca_7500", "acb_8000", "acc_8000", "acd_10000", "ace_10000",
			"acg_10000", "ach_10000", "acj_11000", "ack_13000", "acl_13000", "acm_14000", "acn_15000", "aco_15000", "acp_17000",
			"acq_17000", "acs_20000", "act_20000", "acu_20000", "acv_20000", "acw_25000", "acz_35000", "adb_50000", "adc_50000",
			"adg_160000", "adh_160000", "adi_160000", "adk_168000", "adl_170000", "adn_190000", "ado_200000", "adp_200000", "adq_200001",
			"adt_1500000", "adu_1600000", "adv_2500000", "ady_3000000", "adz_3000000", "aea_3500000", "aec_6500000", "aed_10000000",
			"aee_10000001", "aef_11000000", "aeg_11000000", "aeh_12000000", "aei_12000000", "aej_12000000", "ael_12000000", "aem_17000000",
			"aen_17000000", "aeo_20000001", "aep_21000000", "aeq_25000000", "aes_25000000", "aev_28000000", "aew_30000000", "aex_31000000",
			"aez_35000000", "afa_35000000", "afb_35000000", "afc_41000001", "afd_44000000", "afe_46000000", "aff_50000000", "afg_50000000",
			"afh_50000000", "afi_50000000", "afj_50000000", "afk_50000000", "afm_59000000", "afn_60000000", "afo_62000000", "afp_63000000",
			"afq_70000000", "afr_72000000", "afs_72000000", "afu_100000000", "afv_100000000", "afw_100000000", "afx_100000000",
			"afy_100000000", "afz_100000000", "agb_140000000", "agc_150000000", "agd_150000000", "agg_200000000", "agh_230000000",
			"agi_250000000", "agj_250000000", "agk_280000000", "agl_300000000", "agm_300000000", "agn_300000000", "ago_300000000",
			"agp_300000000", "agq_300000000", "agr_300000000", "ags_300000000", "agt_320000000", "agu_320000001", "agv_400000000",
			"agw_400000000", "agx_400000000", "agy_420000000", "agz_450000000", "aha_450000000", "ahb_600000000", "ahc_650000000",
			"ahd_1000000000", "ahe_2000000000", "ahf_3000000000", "ahh_4000000000", "ahi_3400000000", "ahj_6000000000", "ahk_7000000000",
			"ahl_10000000000", "ahm_12000000000" };

	public static final String[] APOD_ADDRESS_TAG = { "ap001006.html", "ap110414.html", "ap070921.html", "ap111208.html", "ap091231.html",
			"ap051124.html", "ap031226.html", "ap060119.html", "ap071006.html", "ap061229.html", "ap050519.html", "ap120114.html",
			"ap050311.html", "ap010729.html", "ap021214.html", "ap050612.html", "ap100614.html", "ap090503.html", "ap080322.html",
			"ap050908.html", "ap070215.html", "ap041101.html", "ap080502.html", "ap111113.html", "ap080813.html", "ap050914.html",
			"ap010903.html", "ap020717.html", "ap050113.html", "ap030808.html", "ap051202.html", "ap000407.html", "ap080726.html",
			"ap061220.html", "ap101121.html", "ap090122.html", "ap020807.html", "ap080313.html", "ap020731.html", "ap130117.html",
			"ap120210.html", "ap070116.html", "ap040603.html", "ap080117.html", "ap080501.html", "ap100501.html", "ap070714.html",
			"ap071005.html", "ap061103.html", "ap111204.html", "ap050421.html", "ap120323.html", "ap110503.html", "ap101009.html",
			"ap120819.html", "ap120115.html", "ap040819.html", "ap110125.html", "ap010712.html", "ap040220.html", "ap090905.html",
			"ap001005.html", "ap080803.html", "ap100403.html", "ap011225.html", "ap010216.html", "ap080124.html", "ap090205.html",
			"ap031222.html", "ap041116.html", "ap050820.html", "ap041230.html", "ap080815.html", "ap081229.html", "ap080110.html",
			"ap110225.html", "ap070515.html", "ap070710.html", "ap060414.html", "ap080731.html", "ap030423.html", "ap101011.html",
			"ap070411.html", "ap020408.html", "ap060302.html", "ap080308.html", "ap050330.html", "ap091226.html", "ap100413.html",
			"ap110113.html", "ap080515.html", "ap050901.html", "ap060612.html", "ap110219.html", "ap110828.html", "ap010911.html",
			"ap080425.html", "ap090727.html", "ap040701.html", "ap080329.html", "ap071017.html", "ap031103.html", "ap050304.html",
			"ap120914.html", "ap050112.html", "ap110330.html", "ap030726.html", "ap130116.html", "ap090408.html", "ap060209.html",
			"ap090313.html", "ap111029.html", "ap080506.html", "ap110715.html", "ap121217.html", "ap120304.html", "ap061105.html",
			"ap100604.html", "ap091109.html", "ap051208.html", "ap031211.html", "ap040426.html", "ap060412.html", "ap010112.html",
			"ap090426.html", "ap071101.html", "ap080213.html", "ap110421.html", "ap090911.html", "ap090209.html", "ap070531.html",
			"ap060118.html", "ap090618.html", "ap090407.html", "ap020502.html", "ap110922.html", "ap070208.html", "ap100822.html",
			"ap110210.html", "ap030114.html", "ap040627.html", "ap080210.html", "ap110629.html", "ap080823.html", "ap080917.html",
			"ap060524.html", "ap020208.html", "ap031105.html" };

	public static final String[] LOCAL_ASTRONOMY = { "mars", "saturn", "neptune", "callisto_moon", "uranus", "titan_moon", "io_moon",
			"ganymede_moon", "earth", "jupiter", "mercury_planet", "europa_moon", "moon", "venus", "sun" };
}
