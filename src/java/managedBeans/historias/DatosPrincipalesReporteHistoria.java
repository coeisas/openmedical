/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managedBeans.historias;

import java.util.List;

/**
 *
 * @author santos
 */
public class DatosPrincipalesReporteHistoria {

    //clase que contiene los datos principales para un registro clinico

    private String tituloRegistro = "";
    private String logoEmpresa = "";
    private String nombres = "";
    private String genero = "";
    private String apellidos = "";
    private String identificacion = "";
    private String edad = "";
    private String fechaNacimiento = "";
    private String lugarNacimiento = "";
    private String fechaRegistro = "";
    private String eps = "";
    private String servicio = "";
    private String colegio = "";
    private String escolaridad = "";
    private String lateridad = "";
    private String estadoCivil = "";
    private String ocupacion = "";
    private String direccion = "";
    private String tel = "";
    private String firmaMedico = "";//url con la firma del medico
    private String medico = "";//nombre, especialidad, registro medico
    private String dirTelEmpresa = "";//direccion telefono empresa
    private String numeroAutorizacion = "";
    private List<DatosAdicionalesReporteHistoria> listaDatosAdicionales;

    public String getTituloRegistro() {
        return tituloRegistro;
    }

    public void setTituloRegistro(String tituloRegistro) {
        this.tituloRegistro = tituloRegistro;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getEdad() {
        return edad;
    }

    public void setEdad(String edad) {
        this.edad = edad;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getLugarNacimiento() {
        return lugarNacimiento;
    }

    public void setLugarNacimiento(String lugarNacimiento) {
        this.lugarNacimiento = lugarNacimiento;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getEps() {
        return eps;
    }

    public void setEps(String eps) {
        this.eps = eps;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getColegio() {
        return colegio;
    }

    public void setColegio(String colegio) {
        this.colegio = colegio;
    }

    public String getEscolaridad() {
        return escolaridad;
    }

    public void setEscolaridad(String escolaridad) {
        this.escolaridad = escolaridad;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getOcupacion() {
        return ocupacion;
    }

    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public List<DatosAdicionalesReporteHistoria> getListaDatosAdicionales() {
        return listaDatosAdicionales;
    }

    public void setListaDatosAdicionales(List<DatosAdicionalesReporteHistoria> listaDatosAdicionales) {
        this.listaDatosAdicionales = listaDatosAdicionales;
    }

    public String getFirmaMedico() {
        return firmaMedico;
    }

    public void setFirmaMedico(String firmaMedico) {
        this.firmaMedico = firmaMedico;
    }

    public String getDirTelEmpresa() {
        return dirTelEmpresa;
    }

    public void setDirTelEmpresa(String dirTelEmpresa) {
        this.dirTelEmpresa = dirTelEmpresa;
    }

    public String getNumeroAutorizacion() {
        return numeroAutorizacion;
    }

    public void setNumeroAutorizacion(String numeroAutorizacion) {
        this.numeroAutorizacion = numeroAutorizacion;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getLogoEmpresa() {
        return logoEmpresa;
    }

    public void setLogoEmpresa(String logoEmpresa) {
        this.logoEmpresa = logoEmpresa;
    }

    public String getLateridad() {
        return lateridad;
    }

    public void setLateridad(String lateridad) {
        this.lateridad = lateridad;
    }

}
