package com.talentyco.shopping.controller;

import com.talentyco.shopping.domain.Article;
import com.talentyco.shopping.domain.CardItem;
import com.talentyco.shopping.domain.ShoppingCart;
import com.talentyco.shopping.domain.User;
import com.talentyco.shopping.service.ArticleService;
import com.talentyco.shopping.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/shopping-cart")
public class ShoppingCartController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @RequestMapping("/cart")
    public String shoppingCart(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ShoppingCart shoppingCart = shoppingCartService.getShoppingCart(user);
        model.addAttribute("cartItemList", shoppingCart.getCardItems());
        model.addAttribute("shoppingCart", shoppingCart);
        return "shoppingCart";
     }
    @RequestMapping("/add-item")
    public String addItem(@ModelAttribute("article") Article article, @RequestParam("qty") String qty,
                          @RequestParam("size") String size, RedirectAttributes attributes, Model model, Authentication authentication) {
        article = articleService.findArticleById(article.getId());
        if (!article.hasStock(Integer.parseInt(qty))) {
            attributes.addFlashAttribute("notEnoughStock", true);
            return "redirect:/article-detail?id="+article.getId();
        }
        User user = (User) authentication.getPrincipal();
        shoppingCartService.addArticleToShoppingCart(article, user, Integer.parseInt(qty), size);
        attributes.addFlashAttribute("addArticleSuccess", true);
        return "redirect:/article-detail?id="+article.getId();
    }

    @RequestMapping("/update-item")
    public String updateItemQuantity(@RequestParam("id") Long cartItemId,
                                     @RequestParam("qty") Integer qty, Model model) {
        CardItem cartItem = shoppingCartService.findCartItemById(cartItemId);
        if (cartItem.canUpdateQty(qty)) {
            shoppingCartService.updateCartItem(cartItem, qty);
        }
        return "redirect:/shopping-cart/cart";
    }

    @RequestMapping("/remove-item")
    public String removeItem(@RequestParam("id") Long id) {
        shoppingCartService.removeCartItem(shoppingCartService.findCartItemById(id));
        return "redirect:/shopping-cart/cart";
    }
}
