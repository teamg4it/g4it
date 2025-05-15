merge into g4it_subscriber(id, name, creation_date, last_update_date) key(id) values
(100, 'SSG', '2023-03-28 10:11:10', '2023-03-28 10:11:10'),
(101, 'OTHER', '2023-03-28 10:11:10', '2023-03-28 10:11:10')
;

merge into g4it_organization(id, name, subscriber_id, creation_date, last_update_date) key(id) values
(101, 'G4IT', 100, '2023-03-28 10:11:10', '2023-03-28 10:11:10'),
(102, 'OTHER', 100, '2023-03-28 10:11:10', '2023-03-28 10:11:10'),
(103, 'SUB_OTHER_1', 101, '2023-03-28 10:11:10', '2023-03-28 10:11:10'),
(104, 'SUB_OTHER_2', 101, '2023-03-28 10:11:10', '2023-03-28 10:11:10')
;

merge into g4it_user(id, email, creation_date, last_update_date) key(id) values
(100, 'username@organization.com', '2023-03-28 10:11:10', '2023-03-28 10:11:10')
;

merge into g4it_user_organization(id, user_id, organization_id, default_flag) values
(100, 100, 101, true)
;

INSERT INTO public.digital_service(uid, name, user_id, organization_id) VALUES ('uid', 'my service test', 100, 101);

INSERT INTO public.ref_Criterion VALUES ('Acidification',	'Air acidification is linked to emissions of nitrogen oxides, sulfur oxides, ammonia and hydrochloric acid. These pollutants turn into acids in the presence of moisture, and their fallout can damage ecosystems as well as buildings.',	'mol H+ eq');
INSERT INTO public.ref_Criterion VALUES ('Climate change',	'Greenhouse gases (GHG) are gaseous components which absorb the infrared radiation emitted by the earths surface 1 and thus contribute to the greenhouse effect.',	'kg CO2 eq');
INSERT INTO public.ref_Criterion VALUES ('Ionising radiation',	'Corresponds to the effects of radioactivity. This impact corresponds to the radioactive waste resulting from the production of nuclear electricity.',	'kBq U-235 eq');
INSERT INTO public.ref_Criterion VALUES ('Particulate matter and respiratory inorganics',	'The presence of small-diameter fine particles in the air - especially those with a diameter of less than 10 microns - represents a human health problem, since their inhalation can cause respiratory and cardiovascular problems.',	'Disease incidence');
INSERT INTO public.ref_Criterion VALUES ('Resource use (minerals and metals)',	'Industrial exploitation leads to a decrease in available resources whose reserves are limited. This indicator assesses the amount of mineral and metallic resources extracted from nature as if they were antimony.',	'kg Sb eq');

INSERT INTO public.terminal VALUES ('uid2', 'uid', 4, 'France', 15, 100, '2023-09-29 14:16:00.650979', '2023-09-29 14:16:00.651979', 5);

INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Acidification',NULL,'OK', 'Version_Calcule',	'1.0', NULL,	0.004625000000000001, 1,NULL, 'Terminal', 'mol H+ eq', NULL, 'SSG',	'uid2','uid',NULL, 	'1970-01-01',	'SSG', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Climate change',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	0.30833333333333335, 1, NULL,	'Terminal',	'kg CO2 eq', NULL, 'SSG','uid2','uid',NULL,		'1970-01-01',	'SSG', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Ionising radiation',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	0.03895833333333333, 1,NULL, 'Terminal',	'kBq U-235 eq', NULL, 'SSG','uid2','uid',NULL, 	'1970-01-01',	'SSG', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Particulate matter and respiratory inorganics',NULL, 'OK',	'Version_Calcule',	'1.0', NULL, 1.6125, 1, NULL, 'Terminal', 'Disease incidence', NULL, 'SSG','uid2','uid',NULL,	'1970-01-01','SSG', '', '');
INSERT INTO public.ind_indicateur_impact_equipement_physique VALUES ('2023-09-29 14:16:06.965', '2023-06-01', 'FIN_DE_VIE',	'Resource use (minerals and metals)',NULL, 'OK',	'Version_Calcule',	'1.0',NULL,	7.979166666666666,	1, NULL, 'Terminal', 'kg Sb eq', NULL, 'SSG', 'uid2','uid',NULL, 	'1970-01-01',	'SSG', '', '');

