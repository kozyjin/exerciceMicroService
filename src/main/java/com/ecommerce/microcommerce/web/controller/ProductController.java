package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.PrixDeVenteException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@Api( description="API pour es opérations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    //Récupérer la liste des produits

    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }


    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")

    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return produit;
    }




    //ajouter un produit
    @PostMapping(value = "/AjouterProduits")

    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) throws PrixDeVenteException {

        Product productAdded =  productDao.save(product);
        if(productAdded.getPrix() == 0 ){
            throw new PrixDeVenteException("le prix de vente est de 0");
        }

        if (productAdded == null)
            return ResponseEntity.noContent().build();// permet de renvoyer le code 204 No Content. build permet de construire le header et ajoute le code choisi.

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.delete(id);
    }

    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }


    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(400);
    }


    @GetMapping(value = "/AfficherProduits")
    public List<Product> afficherProduits(){

        return productDao.findAll();
    }


    //calcule la marge de chaque produit (différence entre prix d'achat et prix de vente)
    @GetMapping(value = "/AdminProduits")
    public List<String> afficherMargeProduit(){

        List<Product> listeDeProduit = new ArrayList<>();
        List<String> listeDeproduitAvecMarge = new ArrayList<>();
        String margeProduit;
        listeDeProduit =  productDao.findAll();

        for(Product produits : listeDeProduit){
            margeProduit = "Le produit "+produits.getNom()+ " à une marge de "+(produits.getPrix()-produits.getPrixAchat());
            listeDeproduitAvecMarge.add(margeProduit);
        }
        return listeDeproduitAvecMarge;
    }

//retourne la liste de tous les produits triés par nom croissant


    @GetMapping(value ="/trierNom")
    public List <Product> trierProduitsParOrdreAlphabetique() {
        return productDao.findAllByOrderByNomDesc() ;
    }


}

