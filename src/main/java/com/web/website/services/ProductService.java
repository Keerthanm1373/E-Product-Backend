package com.web.website.services;

import com.web.website.models.Products;
import com.web.website.repo.ProductRepo;
import dto.Product_dto;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Component
public class ProductService {
    @Autowired
    private ProductRepo repo;

    @Value("${RAPID_API_KEY}")
    private String RAPID_API_KEY;

    @Value("${GEMINI_API_KEY}")
    private String GEMINE_API_KEY;

    public List<Products> getAllProduct() {
        return repo.findAll();
    }

    public Optional<Products> getProductId(Long id) {
        return repo.findById(id);
    }

    public Products updateProduct(Products product) {
        return repo.save(product);
    }

    public List<Product_dto> getProduct() {
        return repo.findAll()
                .stream().map(this::convertEntityToDTO).collect(Collectors.toList());
    }


    private Product_dto convertEntityToDTO(Products products) {
        Product_dto dto = new Product_dto();
        dto.setId(products.getId());
        dto.setImageName(products.getImageName());
        dto.setName(products.getName());
        dto.setPrice(products.getPrice());
        dto.setProductAvailable(products.getProductAvailable());
        dto.setCategory(products.getCategory());
        return dto;
    }

    public Products saveProduct(Products product, MultipartFile imageFile) throws IOException {
        product.setImageName(imageFile.getOriginalFilename());
        return repo.save(product);
    }

    @Transactional
    public void fetchAndSaveProductsFromAPI(String searchQuery, String productTitle) throws JSONException {
        String searchUrl = "https://real-time-product-search.p.rapidapi.com/search?q=" + searchQuery + "&country=in&language=en";
        String detailUrlBase = "https://real-time-product-search.p.rapidapi.com/product-details?country=in&language=en&product_id=";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", RAPID_API_KEY);
        headers.set("X-RapidAPI-Host", "real-time-product-search.p.rapidapi.com");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

        try {
            ResponseEntity<String> searchResponse = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);

            if (searchResponse.getStatusCode() == HttpStatus.OK) {
                JSONObject json = new JSONObject(searchResponse.getBody());
                JSONArray products = json.getJSONObject("data").getJSONArray("products");

                for (int i = 0; i < products.length(); i++) {
                    JSONObject productJson = products.getJSONObject(i);
                    String title = productJson.optString("product_title", "");

                    double similarityScore = similarity.apply(title.toLowerCase(), productTitle.toLowerCase());
                    System.out.println("Comparing: " + title + " | Similarity: " + similarityScore);

                    if (similarityScore >= 0.60) {
                        String productId = productJson.optString("product_id", "");

                        System.out.println("Matching product found: " + title);
                        System.out.println("Product ID: " + productId);

                        if (!productId.contains(":") && !productId.contains(",") && !productId.contains("|")) {
                            String detailUrl = detailUrlBase + productId;
                            System.out.println("Calling: " + detailUrl);

                            try {
                                ResponseEntity<String> detailResponse = restTemplate.exchange(detailUrl, HttpMethod.GET, entity, String.class);

                                if (detailResponse.getStatusCode() == HttpStatus.OK) {
                                    System.out.println("Detail response received.");
                                    JSONObject detailJson = new JSONObject(detailResponse.getBody()).getJSONObject("data");

                                    Products product = new Products();
                                    product.setName(detailJson.optString("product_title", "No Name"));

                                    JSONObject offer = detailJson.optJSONObject("offer");
                                    String priceString = (offer != null) ? offer.optString("price", "0") : "0";
                                    product.setPrice(Double.parseDouble(priceString.replaceAll("[^\\d.,]", "").replace(",", "")));


                                    product.setRating(detailJson.optDouble("product_rating", 0.0));
                                    product.setReviewCount(detailJson.optInt("product_num_reviews", 0));
                                    product.setUrl(detailJson.optString("product_page_url", ""));
                                    product.setImageName(detailJson.getJSONArray("product_photos").optString(0, ""));

                                    String description = detailJson.optString("product_description", null);
                                    if (description == null || description.isBlank()) {
                                        description = detailJson.optString("description", null);
                                    }
                                    if (description == null || description.isBlank()) {
                                        description = detailJson.optString("product_specs", null);
                                    }
                                    if (description == null || description.isBlank()) {
                                        description = "No description available";
                                    }
                                    product.setDescription(description);
                                    product.setProductAvailable(true);

                                    repo.save(product);
                                    System.out.println("Product saved via detail API: " + product.getName());
                                    break;
                                }
                            } catch (Exception e) {
                                System.err.println("Detail fetch failed: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        // Fallback from search result
                        try {
                            Products product = new Products();
                            product.setName(productJson.optString("product_title", "No Name"));
                            product.setImageName(productJson.getJSONArray("product_photos").optString(0, ""));
                            JSONObject offer = productJson.optJSONObject("offer");
                            String priceString = (offer != null) ? offer.optString("price", "0") : "0";
                            product.setPrice(Double.parseDouble(priceString.replaceAll("[^\\d.,]", "").replace(",", "")));
                            product.setRating(productJson.optDouble("product_rating", 0.0));
                            product.setReviewCount(productJson.optInt("product_num_reviews", 0));
                            product.setUrl(productJson.optString("product_page_url", ""));
                            product.setDescription("Fetched from search result");
                            product.setProductAvailable(true);


                            // Enhance with Gemini
                            String prompt = "Product name: \"" + product.getName() + "\"\n\n"
                                    + "Category (one word): <Provide one single word representing the product category>\n\n"
                                    + "Improved Description (one paragraph, no bullet points or lists): <Provide a concise and engaging one-paragraph description of the product.>";
                            String geminiResult = callGeminiAPI(prompt);

                            if (geminiResult != null && !geminiResult.isBlank()) {
                                String[] parts = geminiResult.split("\n\n");
                                for (String part : parts) {
                                    if (part.toLowerCase().startsWith("category")) {
                                        product.setCategory(part.substring(part.indexOf(":") + 1).trim());
                                    } else if (part.toLowerCase().startsWith("improved description")) {
                                        product.setDescription(part.substring(part.indexOf(":") + 1).trim());
                                    }
                                }
                            }


                            repo.save(product);
                            System.out.println("Product saved via fallback from search data: " + product.getName());
                        } catch (Exception e) {
                            System.err.println("Fallback save failed: " + e.getMessage());
                            e.printStackTrace();
                        }

                        break;
                    }
                }
            } else {
                throw new RuntimeException("Search API failed with status: " + searchResponse.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("Error during fetch and save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            String GEMINI_API_KEY = GEMINE_API_KEY;
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject requestBody = new JSONObject();
            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(new JSONObject().put("text", prompt)));
            requestBody.put("contents", new JSONArray().put(content));

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject responseBody = new JSONObject(response.getBody());
                if (responseBody.has("candidates") && responseBody.getJSONArray("candidates").length() > 0) {
                    JSONObject candidate = responseBody.getJSONArray("candidates").getJSONObject(0);
                    if (candidate.has("content") && candidate.getJSONObject("content").has("parts") && candidate.getJSONObject("content").getJSONArray("parts").length() > 0) {
                        return candidate.getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                    }
                }
                System.err.println("Gemini API response structure might have changed.");
                return null;
            } else {
                System.err.println("Gemini API error: " + response.getStatusCode());
                // Optionally log the full response body for more details:
                // System.err.println("Response body: " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
        }
        return null;
    }
}