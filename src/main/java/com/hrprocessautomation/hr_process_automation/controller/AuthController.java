package com.hrprocessautomation.hr_process_automation.controller;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hrprocessautomation.hr_process_automation.model.Offer;
import com.hrprocessautomation.hr_process_automation.model.User;
import com.hrprocessautomation.hr_process_automation.service.BulkOfferService;
import com.hrprocessautomation.hr_process_automation.service.CsvOfferService;
import com.hrprocessautomation.hr_process_automation.service.OfferPdfService;
import com.hrprocessautomation.hr_process_automation.service.OfferService;
import com.hrprocessautomation.hr_process_automation.service.UserService;

@Controller
public class AuthController {
	@Autowired
	private UserService userService;

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@Autowired
	private OfferService offerService;

	@Autowired
	private OfferPdfService pdfService;
	
	@Autowired
    private CsvOfferService csvOfferService;

    @Autowired
    private BulkOfferService bulkOfferService;

	@PostMapping("/login")
	public String loginUser(@RequestParam String username, @RequestParam String password, Model model) {
		User user = userService.findByUsername(username);
		if (user != null && user.getPassword().equals(password)) {
			// Login successful
			return "home";
		} else {
			// Login failed
			model.addAttribute("error", "Invalid username or password");
			return "login";
		}
	}

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	@PostMapping("/register")
	public String registerUser(@ModelAttribute User user, Model model) {
		if (userService.exists(user.getUsername())) {
			model.addAttribute("error", "Username already exists");
			return "register";
		}
		userService.saveUser(user);
		return "redirect:/login";
	}

	@GetMapping("/home")
	public String home() {
		return "home";
	}

	@GetMapping("/offer/manual")
	public String manualOffer(Model model) {
		model.addAttribute("offer", new Offer());
		return "manual-offer";
	}

	@PostMapping("/offer/manual")
	public String sendManualOffer(@ModelAttribute Offer offer, Model model) {
		offerService.saveAndSendOffer(offer);
		model.addAttribute("success", "Offer saved and email sent successfully!");
		model.addAttribute("offer", new Offer());
		return "manual-offer";
	}

	@PostMapping("/offer/preview")
	public String previewOffer(@ModelAttribute Offer offer, Model model) {

		// Build email HTML
		String emailHtml = buildEmailHtml(offer);

		// Generate PDF (byte[])
		byte[] pdfBytes = pdfService.generateOfferPdf(offer);

		model.addAttribute("offer", offer);
		model.addAttribute("emailHtml", emailHtml);
		model.addAttribute("pdfBase64", Base64.getEncoder().encodeToString(pdfBytes));
		
		offerService.saveOrUpdateOffer(offer);
		return "offer-preview";
	}

	@PostMapping("/offer/send")
	public String sendOffer(@ModelAttribute Offer offer) {

		offerService.saveAndSendOffer(offer); // DB + Email + PDF
		return "redirect:/home";
	}

	private String buildEmailHtml(Offer offer) {
		return """
				<h2>Offer Letter</h2>
				<p>Dear %s,</p>
				<p>We are pleased to offer you the position of <b>%s</b>.</p>
				<p>Salary: <b>₹%s</b></p>
				<p>Regards,<br/>HR Team</p>
				""".formatted(offer.getName(), offer.getPosition(), offer.getSalary());
	}

	@PostMapping("/send-offer")
	public String sendOffer(@ModelAttribute Offer offer, Model model) {
		offerService.saveAndSendOffer(offer);
		model.addAttribute("message", "Offer sent successfully!");
		return "redirect:/home"; // Or wherever you want to go
	}

	@PostMapping("/manual-offer")
	public String saveManualOffer(@ModelAttribute Offer offer) {
		offerService.saveOrUpdateOffer(offer); // save or update
		return "redirect:/offer/preview/" + offer.getId(); // redirect to preview
	}

	@GetMapping("/offer/edit/{id}")
	public String editOffer(@PathVariable Long id, Model model) {
		Offer offer = offerService.getOfferById(id); // fetch the offer from DB
		model.addAttribute("offer", offer); // send it to the form page
		return "manual-offer"; // show manual-offer.html
	}
	
	@GetMapping("/bulk-offer")
    public String bulkOfferPage() {
        return "bulkOffer";
    }

    @PostMapping("/bulk-offer/send")
    public String sendBulkOffer(@RequestParam("file") MultipartFile file) throws Exception {

        List<Offer> offers = csvOfferService.readOffers(file);
        bulkOfferService.processAndSendOffers(offers);

        return "redirect:/home";
    }
    
    @GetMapping("/offer/respond/{id}")
    public String respondToOffer(
            @PathVariable Long id,
            @RequestParam String action,
            Model model) {

    	Offer offer = offerService.getOfferById(id);
    	if (offer == null) {
    	    throw new RuntimeException("Offer not found");
    	}


        if ("accept".equalsIgnoreCase(action)) {
            offer.setcandidateResponse(action);
        } else if ("reject".equalsIgnoreCase(action)) {
            offer.setcandidateResponse("REJECTED");
        }

        offerService.saveOrUpdateOffer(offer);

        model.addAttribute("status", offer.getcandidateResponse());
        model.addAttribute("name", offer.getName());

        return "offerResponse";
    }
    
    @GetMapping("/candidates")
    public String viewCandidates(
            @RequestParam(defaultValue = "ALL") String status,
            Model model) {

        List<Offer> offers;

        if (status.equals("ALL")) {
            offers = offerService.getAllOffers();
        } else if(status.equals("ACCEPTED")) {
            offers = offerService.getOffersByResponse(status);
        }else if(status.equals("REJECTED")) {
        	 offers = offerService.getOffersByResponse(status);
        }else {
        	offers = offerService.getAllOffers();
        }

        model.addAttribute("offers", offers);
        model.addAttribute("selectedStatus", status);

        return "candidates";
    }


}
