package ase.cogniprice.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "product_image")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] image;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] thumbnail;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = true)
    private Long size;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}

