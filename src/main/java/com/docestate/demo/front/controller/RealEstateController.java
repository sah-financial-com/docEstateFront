package com.docestate.demo.front.controller;

import com.docestate.demo.front.dto.RealEstateDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RealEstateController {

	private static final Logger LOG = LoggerFactory.getLogger(RealEstateController.class);

	private static final String DOCESTATE_REST_API         = "http://localhost:8081";
	private static final String DOCESTATE_REAL_ESTATE_LIST = "/real-estates";
	private static final String DOCESTATE_REAL_ESTATE      = "/real-estate";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@GetMapping("/")
	public String listAll(Model model) throws IOException {
		Content  content  = Request.Get(DOCESTATE_REST_API + DOCESTATE_REAL_ESTATE_LIST).execute().returnContent();
		JsonNode jsonNode = objectMapper.readTree(content.asString(StandardCharsets.UTF_8));
		model.addAttribute("realEstateList", extractRealEstates(jsonNode));

		return "home";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable("id") Long id, Model model) throws IOException {
		Content  content  = Request.Get(DOCESTATE_REST_API + DOCESTATE_REAL_ESTATE + "/" + id).execute().returnContent();
		JsonNode jsonNode = objectMapper.readTree(content.asString(StandardCharsets.UTF_8));
		model.addAttribute("realEstate", extractRealEstate(jsonNode));

		return "createOrEdit";
	}

	@GetMapping("/edit")
	public String createNew(Model model) {
		model.addAttribute("realEstate", new RealEstateDTO("", "", ""));

		return "createOrEdit";
	}

	@PostMapping("/persist")
	public String greetingSubmit(@ModelAttribute RealEstateDTO realEstate, Model model) throws IOException {
		Request request;

		if (realEstate.getId().isBlank()) {
			request = Request.Post(DOCESTATE_REST_API + DOCESTATE_REAL_ESTATE);
		} else {
			request = Request.Put(DOCESTATE_REST_API + DOCESTATE_REAL_ESTATE + "/" + realEstate.getId());
		}
		request.bodyString(objectMapper.writeValueAsString(realEstate), ContentType.APPLICATION_JSON).execute();

		return listAll(model);
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable("id") Long id, Model model) throws IOException {
		Request.Delete(DOCESTATE_REST_API + DOCESTATE_REAL_ESTATE + "/" + id).execute();
		return listAll(model);
	}

	private List<RealEstateDTO> extractRealEstates(JsonNode json) {
		List<RealEstateDTO> realEstates = new ArrayList<>();
		if (json.isArray()) {
			ArrayNode realEstateArray = (ArrayNode) json;
			for (int i = 0; i < realEstateArray.size(); i++) {
				realEstates.add(extractRealEstate(realEstateArray.get(i)));
			}
		}
		return realEstates;
	}

	private RealEstateDTO extractRealEstate(JsonNode jsonNode) {
		String id      = jsonNode.get("id").asText();
		String name    = jsonNode.get("name").asText();
		String address = jsonNode.get("address").asText();

		return new RealEstateDTO(id, name, address);
	}
}
