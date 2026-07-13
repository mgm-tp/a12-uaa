/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.uaa.authorization.properties;

public class PropertyComparatorTest {

	//	private static Javers JAVERS = JaversBuilder.javers().withPrettyPrint(true).build();
	//	private static final String xmlXPath = "/DomainReleaseBundle[1]/releaseBundle[1]/bundles[3]/ticket[1]/smartUrl[1]/prettyUrlName[1]";
	//
	//	private static String extractPathViaObjectChange(String affectGlobalId, String masterObjectId) {
	//		return affectGlobalId.replace(masterObjectId + "/#", "");
	//	}
	//
	//	private static final List<String> pathPropertyAuthorizationConfiguration = Arrays.asList("addressObjects[0].streetName",
	//		"addressObjects[1].number",
	//		"childrenAndAge/Vi.age",
	//		"childrenAndAge/Hung.name");
	//
	//	public void compareJavaObjects() {
	//		Map<String, String> javersPathInputPath = new HashMap<>();
	//		for (String pathInput : pathPropertyAuthorizationConfiguration) {
	//			String pathNormalize = pathInput.replace("[", "/");
	//			if (pathInput.contains("].")) {
	//				pathNormalize = pathNormalize.replace("].", ".");
	//			} else if (pathInput.contains("]")) {
	//				pathNormalize = pathNormalize.replace("]", "");
	//			}
	//			javersPathInputPath.put(pathNormalize, pathInput);
	//		}
	//
	//		Javers javers = JaversBuilder.javers().withPrettyPrint(true).build();
	//		Diff diff = javers.compare(ExampleDataFactory.getOldFamilyObject(), ExampleDataFactory.getNewFamilyObject());
	//		List<Change> changes = diff.getChanges();
	//		List<String> pathsChange = new ArrayList<>();
	//		for (Change e : changes) {
	//			String changePath = "";
	//			if (e instanceof PropertyChange change) {
	//				changePath = change.getPropertyNameWithPath();
	//			}
	//			if (e instanceof NewObject) {
	//				changePath = extractPathViaObjectChange(e.getAffectedGlobalId().value(),
	//					e.getAffectedGlobalId().masterObjectId().getTypeName());
	//			}
	//			if (e instanceof ObjectRemoved) {
	//				changePath = extractPathViaObjectChange(e.getAffectedGlobalId().value(),
	//					e.getAffectedGlobalId().masterObjectId().getTypeName());
	//			}
	//			pathsChange.add(changePath);
	//		}
	//
	//		if (!pathsChange.isEmpty() && !CollectionUtils.containsAny(pathsChange, javersPathInputPath.keySet())) {
	//			System.out.println("You are fine to go");
	//		} else {
	//			ArrayList<String> readOnlyPaths = new ArrayList<String>(javersPathInputPath.keySet());
	//			readOnlyPaths.retainAll(pathsChange);
	//			System.out.println("You are modifying something which is not belong to you ");
	//			for (String javerPathChange : readOnlyPaths) {
	//				System.out.println(javersPathInputPath.get(javerPathChange));
	//			}
	//		}
	//
	//	}
	//
	//	public void maskingJavaObject() {
	//		Family family = ExampleDataFactory.getNewFamilyObject();
	//		try {
	//			Map<String, String> beanUtilsPathInputPath = new HashMap<>();
	//			for (String pathInput : pathPropertyAuthorizationConfiguration) {
	//				String pathNormalize = pathInput.replace("/", ".");
	//				beanUtilsPathInputPath.put(pathNormalize, pathInput);
	//			}
	//			for (String pathNormalize : beanUtilsPathInputPath.keySet()) {
	//				System.out.println(pathNormalize);
	//				BeanUtils.setProperty(family, pathNormalize, "********");
	//				//				PropertyUtils.setProperty(family, pathNormalize, null);
	//			}
	//			System.out.println(family.toString());
	//		} catch (Exception e) {
	//			System.out.println(e);
	//		}
	//
	//	}
	//
	//	public void compareA12JsonDocuments() {
	//		try {
	//			File file1 = new File("src/test/resources/sample-1.json");
	//			File file2 = new File("src/test/resources/sample-2.json");
	//			FileInputStream fileInputStream = new FileInputStream(file1);
	//			FileInputStream fileInputStream2 = new FileInputStream(file2);
	//			ObjectMapper mapper = new ObjectMapper();
	//			compareJsonDocuments(mapper.readTree(fileInputStream), mapper.readTree(fileInputStream2));
	//		} catch (Exception e) {
	//			System.out.println(e);
	//		}
	//	}
	//
	//	private static final String jsonPointerAsString = """
	//		[\
	//		{"op":"replace","path":"/DomainReleaseBundle/releaseBundle/bundles/0/releases/1/comment","value":"***********************"}\
	//		,\
	//		{"op":"replace","path":"/DomainReleaseBundle/releaseBundle/bundles/0/releases/2/comment","value":"***********************"}\
	//		]\
	//		""";

	//	public void maskSensitiveDataJson() {
	//		try {
	//			File file3 = new File("src/test/resources/NestedWillBeChanged.json");
	//			FileInputStream fileInputStream3 = new FileInputStream(file3);
	//
	//			final ObjectMapper mapperModify = new ObjectMapper();
	//			JsonNode patchApply = mapperModify.readTree(jsonPointerAsString);
	//			JsonNode jsonNodeWillBeChanged = mapperModify.readTree(fileInputStream3);
	//

	////			JsonPatch.applyInPlace(patchApply, jsonNodeWillBeChanged);
	//			mapperModify.enable(SerializationFeature.INDENT_OUTPUT);
	//			mapperModify.writeValue(new File("target/NestedChanged.json"), jsonNodeWillBeChanged);
	//		} catch (Exception e) {
	//			System.out.println(e);
	//		}
	//	}
	//	public void compareA12JSONNestedDocuments() {
	//		try {
	//			File file1 = new File("src/test/resources/Nested.json");
	//			File file2 = new File("src/test/resources/Nested1.json");
	//
	//			FileInputStream fileInputStream = new FileInputStream(file1);
	//			FileInputStream fileInputStream2 = new FileInputStream(file2);
	//
	//			ObjectMapper mapper = new ObjectMapper();
	//			compareJsonDocuments(mapper.readTree(fileInputStream), mapper.readTree(fileInputStream2));
	//		} catch (Exception e) {
	//			System.out.println(e);
	//		}
	//	}

	//	public void compareJsonDocuments(JsonNode oldJsonDocument, JsonNode newJsonDocument) {
	//		final JsonNode jsonNode = JsonDiff.asJson(oldJsonDocument, newJsonDocument);
	//		for (int i = 0; i < jsonNode.size(); i++) {
	//			System.out.println(jsonNode.get(i));
	//		}
	//	}

	//	public void compareXMLA12Documents() {
	//		try {
	//			File file1 = new File("src/test/resources/ReleaseBundleDocument.xml");
	//			File file2 = new File("src/test/resources/ReleaseBundleDocument2.xml");
	//			FileInputStream fileInputStream = new FileInputStream(file1);
	//			FileInputStream fileInputStream2 = new FileInputStream(file2);
	//			compareXmlDocuments(fileInputStream, fileInputStream2);
	//		} catch (Exception e) {
	//			System.out.println(e);
	//		}
	//	}
	//
	//	public void compareXmlDocuments(InputStream oldDocument, InputStream newDocument) {
	//		org.xmlunit.diff.Diff diff = DiffBuilder.compare(oldDocument).withTest(newDocument).build();
	//		for (Difference difference : diff.getDifferences()) {
	//			System.out.println(difference.getComparison().getControlDetails().getParentXPath());
	//			System.out.println(difference.getComparison().getControlDetails().getValue());
	//		}
	//	}
	//
	//	public void maskSensitiveDataXML() {
	//		try {
	//			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
	//			DocumentBuilder b = f.newDocumentBuilder();
	//			Document doc = b.parse(new File("src/test/resources/ReleaseBundleDocumentWillBeChanged.xml"));
	//
	//			XPath xPath = XPathFactory.newInstance().newXPath();
	//			Node startDateNode = (Node) xPath.compile(xmlXPath).evaluate(doc, XPathConstants.NODE);
	//			startDateNode.setTextContent("A12-1111111111111");
	//
	//			Transformer tf = TransformerFactory.newInstance().newTransformer();
	//			tf.setOutputProperty(OutputKeys.INDENT, "yes");
	//			tf.setOutputProperty(OutputKeys.METHOD, "xml");
	//			tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	//
	//			DOMSource domSource = new DOMSource(doc);
	//			StreamResult sr = new StreamResult(new File("target/ReleaseBundleDocumentChanged.xml"));
	//			tf.transform(domSource, sr);
	//		} catch (Exception e) {
	//			System.out.println(e);
	//		}
	//	}
	//
	//	public void checkJaversRepository() throws Exception {
	//		Family familyObject = ExampleDataFactory.getOldFamilyObject();
	//		//		DataHolder holder = new DataHolder(UUID.randomUUID().toString(), familyObject);
	//		Commit commit = JAVERS.commit("test", familyObject);
	//
	//		familyObject.setDescription("Changed");
	//		//Commit commit2 = JAVERS.commit("test", familyObject);
	//		JqlQuery query = QueryBuilder.byClass(Family.class).withCommitId(commit.getId().valueAsNumber()).build();
	//		//		JqlQuery query = QueryBuilder.byClass(Family.class).build();
	//		List<Shadow<Family>> shadows = JAVERS.findShadows(query);
	//
	//		Family originalObject = shadows.get(0).get();
	//
	//		//		Changes changes = JAVERS.findChanges(query);
	//		Diff diff = JAVERS.compare(originalObject, familyObject);
	//		List<Change> changes = diff.getChanges();
	//
	//		List<String> pathsChange = new ArrayList<>();
	//		for (Change e : changes) {
	//			String changePath = "";
	//			if (e instanceof PropertyChange change) {
	//				changePath = change.getPropertyNameWithPath();
	//			}
	//			if (e instanceof NewObject) {
	//				changePath = extractPathViaObjectChange(e.getAffectedGlobalId().value(),
	//					e.getAffectedGlobalId().masterObjectId().getTypeName());
	//			}
	//			if (e instanceof ObjectRemoved) {
	//				changePath = extractPathViaObjectChange(e.getAffectedGlobalId().value(),
	//					e.getAffectedGlobalId().masterObjectId().getTypeName());
	//			}
	//			pathsChange.add(changePath);
	//		}
	//
	//		System.out.println();
	//	}
	//
	//	public static class DataHolder {
	//		@Id
	//		private String id;
	//		private Object data;
	//
	//		public DataHolder(String id, Object data) {
	//			this.id = id;
	//			this.data = data;
	//		}
	//
	//		public Object getData() {
	//			return data;
	//		}
	//
	//	}

}
