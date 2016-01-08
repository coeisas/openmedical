/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo.entidades;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Mario
 */
@Entity
@Table(name = "cfg_proposito_consulta", catalog = "openmedical", schema = "public")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CfgPropositoConsulta.findAll", query = "SELECT c FROM CfgPropositoConsulta c"),
    @NamedQuery(name = "CfgPropositoConsulta.findByIdProposito", query = "SELECT c FROM CfgPropositoConsulta c WHERE c.idProposito = :idProposito"),
    @NamedQuery(name = "CfgPropositoConsulta.findByDescripcion", query = "SELECT c FROM CfgPropositoConsulta c WHERE c.descripcion = :descripcion")})
public class CfgPropositoConsulta implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_proposito", nullable = false)
    private Integer idProposito;
    @Column(name = "descripcion", length = 50)
    private String descripcion;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cfgPropositoConsulta")
    private List<CitPropositoConsulta> citPropositoConsultaList;
    @Transient
    private boolean valor;

    public CfgPropositoConsulta() {
    }

    public CfgPropositoConsulta(Integer idProposito) {
        this.idProposito = idProposito;
    }

    public Integer getIdProposito() {
        return idProposito;
    }

    public void setIdProposito(Integer idProposito) {
        this.idProposito = idProposito;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @XmlTransient
    public List<CitPropositoConsulta> getCitPropositoConsultaList() {
        return citPropositoConsultaList;
    }

    public void setCitPropositoConsultaList(List<CitPropositoConsulta> citPropositoConsultaList) {
        this.citPropositoConsultaList = citPropositoConsultaList;
    }
    
    public boolean isValor() {
        return valor;
    }

    public void setValor(boolean valor) {
        this.valor = valor;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProposito != null ? idProposito.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CfgPropositoConsulta)) {
            return false;
        }
        CfgPropositoConsulta other = (CfgPropositoConsulta) object;
        if ((this.idProposito == null && other.idProposito != null) || (this.idProposito != null && !this.idProposito.equals(other.idProposito))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.CfgPropositoConsulta[ idProposito=" + idProposito + " ]";
    }
    
}
