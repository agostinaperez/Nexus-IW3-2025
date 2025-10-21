package edu.iua.nexus.auth.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role implements Serializable {

	private static final long serialVersionUID = -845420067971973620L;
	@Column(nullable = false, length = 100)
	private String description;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
//hay una diferencia entre el identity y el sequence. EN este el jpa no hace nada y la BD se encarga de auto incrementar. EN el sequence jpa busca el valor, lo incrementa y lo asigna
	private int id;
	@Column(unique = true, nullable = false)
	private String name;
}