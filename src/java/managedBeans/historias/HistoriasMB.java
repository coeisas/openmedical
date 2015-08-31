package managedBeans.historias;

import beans.utilidades.EstructList;
import beans.utilidades.LazyPacienteDataModel;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import beans.utilidades.MetodosGenerales;
import beans.utilidades.NodoArbolHistorial;
import beans.utilidades.TipoNodoEnum;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import managedBeans.Citas.AgendaPrestadorMB;
import managedBeans.seguridad.LoginMB;
import modelo.entidades.CfgClasificaciones;
import modelo.entidades.CfgDiagnostico;
import modelo.entidades.CfgEmpresa;
import modelo.entidades.CfgMaestrosTxtPredefinidos;
import modelo.entidades.CfgPacientes;
import modelo.entidades.CfgTxtPredefinidos;
import modelo.entidades.CfgUsuarios;
import modelo.entidades.CitCitas;
import modelo.entidades.FacServicio;
import modelo.entidades.HcCamposReg;
import modelo.entidades.HcDetalle;
import modelo.entidades.HcRegistro;
import modelo.entidades.HcTipoReg;
import modelo.fachadas.CfgClasificacionesFacade;
import modelo.fachadas.CfgDiagnosticoFacade;
import modelo.fachadas.CfgEmpresaFacade;
import modelo.fachadas.CfgMaestrosTxtPredefinidosFacade;
import modelo.fachadas.CfgPacientesFacade;
import modelo.fachadas.CfgTxtPredefinidosFacade;
import modelo.fachadas.CfgUsuariosFacade;
import modelo.fachadas.CitCitasFacade;
import modelo.fachadas.CitTurnosFacade;
import modelo.fachadas.FacServicioFacade;
import modelo.fachadas.HcCamposRegFacade;
import modelo.fachadas.HcRegistroFacade;
import modelo.fachadas.HcTipoRegFacade;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
import java.util.Calendar;

@ManagedBean(name = "historiasMB")
@SessionScoped
public class HistoriasMB extends MetodosGenerales implements Serializable {

    @ManagedProperty(value = "#{agendaPrestadorMB}")
    private AgendaPrestadorMB agendaPrestadorMB;
    //---------------------------------------------------
    //-----------------FACHADAS -------------------------
    //---------------------------------------------------
    @EJB
    HcRegistroFacade registroFacade;
    @EJB
    CitTurnosFacade turnosFacade;
    @EJB
    CitCitasFacade citasFacade;
    @EJB
    HcTipoRegFacade tipoRegCliFacade;
    @EJB
    HcCamposRegFacade camposRegFacade;
    @EJB
    CfgPacientesFacade pacientesFacade;
    @EJB
    CfgMaestrosTxtPredefinidosFacade maestrosTxtPredefinidosFacade;
    @EJB
    CfgTxtPredefinidosFacade txtPredefinidosFacade;
    @EJB
    CfgDiagnosticoFacade diagnosticoFacade;
    @EJB
    CfgUsuariosFacade usuariosFacade;
    @EJB
    CfgClasificacionesFacade clasificacionesFacade;
    @EJB
    FacServicioFacade servicioFacade;
    @EJB
    CfgEmpresaFacade empresaFacade;
    @EJB
    CfgPacientesFacade pacientesFachada;

    //---------------------------------------------------
    //-----------------ENTIDADES ------------------------
    //---------------------------------------------------
    private HcTipoReg tipoRegistroClinicoActual;
    //private List<CfgPacientes> listaPacientes;
    private LazyDataModel<CfgPacientes> listaPacientes;
    //private List<CfgPacientes> listaPacientesFiltered;
    private CfgPacientes pacienteSeleccionadoTabla;
    private CfgPacientes pacienteSeleccionado;
    private CfgPacientes pacienteTmp;
    private List<CfgMaestrosTxtPredefinidos> listaMaestrosTxtPredefinidos;
    private List<CfgTxtPredefinidos> listaTxtPredefinidos;
    private List<CfgUsuarios> listaPrestadores;
    private CfgTxtPredefinidos txtPredefinidoActual = null;
    private CfgClasificaciones clasificacionBuscada;
    private CfgDiagnostico diagnosticoBuscado;
    private FacServicio servicioBuscado;
    //---------------------------------------------------
    //-----------------VARIABLES ------------------------
    //---------------------------------------------------        

    private String filtroAutorizacion = "";
    private Date filtroFechaInicial = null;//new Date();
    private Date filtroFechaFinal = null;//new Date();

    private String detalleTextoPredef = "";
    private String idTextoPredef = "";
    private String idMaestroTextoPredef = "";
    private String nombreTextoPredefinido = "";
    private String urlFoto = "../recursos/img/img_usuario.png";
    private String tipoRegistroClinico = "";//tipo de registro clinico usado para cargar el fomulario correspondiente
    private String[] regCliSelHistorial;//registros clinicos seleccionados para el historial
    private String urlPagina = "";
    private String nombreTipoRegistroClinico = "";
    private String identificacionPaciente = "";
    private String tipoIdentificacion = "";
    private String nombrePaciente = "Paciente";
    private String generoPaciente = "";
    private String edadPaciente = "";
    private String administradoraPaciente = "";
    private String idEditorSeleccionado = "";//determinar cual de los editores de texto se les asignara un texto predefinido
    private int posListaTxtPredef = 0;//determinar la posicion en la estructura 'estructuraCampos' al usar un texto predefinido
    private String idPrestador = "";
    private String especialidadPrestador = "";
    private EstructList estructuraCampos = new EstructList();//valores de cada uno de los campos de cualquier registro clinico
    private Date fechaReg;
    private Date fechaSis;
    private TreeNode treeNodeRaiz;//nodos raiz del historico
    //private TreeNode treeNodeSeleccionado;//nodo seleccionado del historico
    private TreeNode[] treeNodesSeleccionados;
    private List<HcTipoReg> listaTipoRegistroClinico;
    private boolean hayPacienteSeleccionado = false;//se ha seleccionado un paciente
    private boolean modificandoRegCli = false;//se esta modificando un registro clinco existente
    private boolean btnHistorialDisabled = true;
    private boolean btnPdfAgrupadoDisabled = true;
    private HcRegistro registroEncontrado = null;//variable que almacena el registro clinico cargado desde el historial
    private List<DatosPrincipalesReporteHistoria> listaRegistrosParaPdf;// maneja como lista por si fueran varias historias al tiempo
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfDateHour = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    //private final SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm:ss");
    private CfgEmpresa empresa;
    private LoginMB loginMB;
    private boolean btnEditarRendered = false;

    private String turnoCita = "";//identificador del turno de la cita
    private boolean cargandoDesdeTab = false;
    private CitCitas citaActual = null;

    //---------------------------------------------------
    //----------------- FUNCIONES INICIALES -----------------------
    //---------------------------------------------------
    public HistoriasMB() {
    }

    @PostConstruct
    public void inicializar() {
        recargarMaestrosTxtPredefinidos();
        //listaPacientes = pacientesFacade.buscarOrdenado();
        //listaPacientesFiltered= pacientesFacade.buscarOrdenado();
        listaPacientes = new LazyPacienteDataModel(pacientesFachada);
        listaTipoRegistroClinico = tipoRegCliFacade.buscarTiposRegstroActivos();
        listaPrestadores = usuariosFacade.buscarUsuariosParaHistorias();
        seleccionaTodosRegCliHis();
        empresa = empresaFacade.findAll().get(0);
    }

    public void cargarDesdeTab(String id) {//cargar paciente y desde Tab agenda prestador                
        cargandoDesdeTab = true;
        turnoCita = "";
        citaActual = null;
        limpiarFormulario();
        String[] splitId = id.split(";");
        if (splitId[0].compareTo("idCita") == 0) {
            citaActual = citasFacade.find(Integer.parseInt(splitId[1]));
            //BUSCAR PACIENTE
            pacienteTmp = pacientesFacade.find(citaActual.getIdPaciente().getIdPaciente());
            if (pacienteTmp == null) {
                imprimirMensaje("Error", "No se encontró el paciente correspondiente a la cita", FacesMessage.SEVERITY_ERROR);
                cargandoDesdeTab = false;
                RequestContext.getCurrentInstance().update("IdFormFacturacion");
                RequestContext.getCurrentInstance().update("IdMensajeFacturacion");
                return;
            }
            pacienteSeleccionadoTabla = pacienteTmp;
            cargarPaciente();
            turnoCita = citaActual.getIdTurno().getIdTurno().toString();
        }
        RequestContext.getCurrentInstance().update("IdMensajeFacturacion");
        cargandoDesdeTab = false;
    }

    //---------------------------------------------------
    //----------------- HISTORICO -----------------------
    //---------------------------------------------------
    private void seleccionaTodosRegCliHis() {//seleccionar todos los tipos de registros clinicos para el historial
        regCliSelHistorial = new String[listaTipoRegistroClinico.size()];
        for (int i = 0; i < listaTipoRegistroClinico.size(); i++) {
            regCliSelHistorial[i] = listaTipoRegistroClinico.get(i).getIdTipoReg().toString();
        }
    }

    public void cargarHistorialCompleto() {
        btnHistorialDisabled = true;
        btnPdfAgrupadoDisabled = true;
//        filtroFechaInicial.setDate(1);
//        filtroFechaInicial.setMonth(c.get(Calendar.MONTH));
//        filtroFechaInicial.setYear(c.get(Calendar.YEAR) - 1900);
//        filtroFechaFinal.setDate(c.get(Calendar.DATE));
//        filtroFechaFinal.setMonth(c.get(Calendar.MONTH));
//        filtroFechaFinal.setYear(c.get(Calendar.YEAR) - 1900);

        seleccionaTodosRegCliHis();//selecciona todos los tipos de registros clinicos
        cargarHistorial();//realiza la creacion del arbol historial
    }

    public void cargarHistorial() {
        //genera un arbol con los registros clinicos de un paciente(depende de los seleccionados en 'IdComboTipReg')

        treeNodeRaiz = new DefaultTreeNode(new NodoArbolHistorial(TipoNodoEnum.RAIZ_HISTORIAL, null, -1, -1, null, null), null);
        NodoArbolHistorial nodoSeleccionTodoNada = new NodoArbolHistorial(TipoNodoEnum.NOVALUE, null, -1, -1, null, "Selección Todo/Ninguno");

        TreeNode nodoInicial = new DefaultTreeNode(nodoSeleccionTodoNada, treeNodeRaiz);
        nodoInicial.setExpanded(true);

        treeNodesSeleccionados = null;
        btnPdfAgrupadoDisabled = true;
        btnHistorialDisabled = true;
        boolean exitenRegistros = false;
        boolean usarFiltroFechas = false;
        boolean usarFiltroAutorizacion = false;

        if (pacienteSeleccionado == null) {
            imprimirMensaje("Error", "No hay un paciente seleccionado", FacesMessage.SEVERITY_ERROR);
            RequestContext.getCurrentInstance().update("IdFormHistorias:IdPanelHistorico");
            return;
        }
        if ((filtroFechaInicial == null && filtroFechaFinal != null) || (filtroFechaInicial != null && filtroFechaFinal == null)) {
            imprimirMensaje("Error", "Debe especificar fecha inicial y fecha final al tiempo ", FacesMessage.SEVERITY_ERROR);
            RequestContext.getCurrentInstance().update("IdFormHistorias:IdPanelHistorico");
            return;
        }
        Calendar aux = null;
        if (filtroFechaInicial != null && filtroFechaFinal != null) {
            //Las fechas filtro tienen como hora las 0. por esta razon no se incluirian los registros de la fecha final. Se aumenta un dia a la fecha
            aux = Calendar.getInstance();
            aux.setTime(filtroFechaFinal);
//            Se aumenta un dia a la fecha final
            aux.add(Calendar.DATE, 1);
            usarFiltroFechas = true;
        }
        List<Integer> idCitas = new ArrayList();
        if (validarNoVacio(filtroAutorizacion)) {
//            se busca los idCita de las citas que se crearon con la autorizacion;
            idCitas = citasFacade.buscarIdCitasByNumAutorizacion(pacienteSeleccionado, filtroAutorizacion);
            usarFiltroAutorizacion = true;
        }
        List<HcRegistro> listaRegistrosPaciente = new ArrayList();
        if (usarFiltroFechas && usarFiltroAutorizacion) {//usar dos filtros
            if (!idCitas.isEmpty()) {
                listaRegistrosPaciente = registroFacade.buscarFiltradoPorNumeroAutorizacionYFecha(pacienteSeleccionado.getIdPaciente(), filtroFechaInicial, aux.getTime(), idCitas);
            }
        } else if (usarFiltroFechas) {// usar filtro de fecha
            listaRegistrosPaciente = registroFacade.buscarOrdenado(pacienteSeleccionado.getIdPaciente(), filtroFechaInicial, aux.getTime());
        } else if (usarFiltroAutorizacion) {//usar filtro de autorizacion          
            if (!idCitas.isEmpty()) {
                listaRegistrosPaciente = registroFacade.buscarFiltradoPorNumeroAutorizacion(pacienteSeleccionado.getIdPaciente(), idCitas);
            }
        } else {//no usar filtro, se busca todos
            listaRegistrosPaciente = registroFacade.buscarOrdenadoPorFecha(pacienteSeleccionado.getIdPaciente());// pacienteSeleccionado.getHcRegistroList();//buscar por orden de fecha decendente            
        }

        NodoArbolHistorial nodHisNuevo;//esta dentro de un TreeNode
        NodoArbolHistorial nodHisFecha;//
        NodoArbolHistorial nodHisComparar;//
        TreeNode treeNodeFecha = null;//son realmente nodos del arbol(contiene a un NodoArbolIstoria)
        TreeNode treeNodeCreado;
        for (HcRegistro registro : listaRegistrosPaciente) {
            if (estaEnListaTipRegHis(registro.getIdTipoReg().getIdTipoReg().toString())) {//verificar que este en la lista de tipos de registro que se desea listar
                exitenRegistros = true;
                nodHisNuevo = new NodoArbolHistorial(
                        TipoNodoEnum.REGISTRO_HISTORIAL,
                        registro.getFechaReg(),
                        registro.getIdRegistro(),
                        registro.getIdTipoReg().getIdTipoReg(),
                        registro.getIdTipoReg().getNombre(),
                        registro.getIdMedico().nombreCompleto());
                if (treeNodeFecha == null) {//nose han agregado nodos
                    nodHisFecha = new NodoArbolHistorial(TipoNodoEnum.FECHA_HISTORIAL, registro.getFechaReg(), -1, -1, null, null);
                    treeNodeFecha = new DefaultTreeNode(nodHisFecha, nodoInicial);
                    treeNodeFecha.setExpanded(true);
                    treeNodeCreado = new DefaultTreeNode(nodHisNuevo, treeNodeFecha);
                } else {//ya existe un nodo de tipo fecha creado con anterioridad                    
                    nodHisComparar = (NodoArbolHistorial) treeNodeFecha.getData();
                    if (nodHisNuevo.getStrFecha().compareTo(nodHisComparar.getStrFecha()) == 0) {//la es la misma se crea un nodo REGISTRO_HISTORIAL al ultimo nodo fecha
                        treeNodeCreado = new DefaultTreeNode(nodHisNuevo, treeNodeFecha);
                    } else {//la fecha ha cambiado, se crea un nodo 'FECHA_HISTORIAL' y se le agrega el nodo REGISTRO_HISTORIAL
                        nodHisFecha = new NodoArbolHistorial(TipoNodoEnum.FECHA_HISTORIAL, registro.getFechaReg(), -1, -1, null, null);
                        treeNodeFecha = new DefaultTreeNode(nodHisFecha, nodoInicial);
                        treeNodeFecha.setExpanded(true);
                        treeNodeCreado = new DefaultTreeNode(nodHisNuevo, treeNodeFecha);
                    }
                }
            }
        }
        if (!exitenRegistros) {//no se encontraron registros
            nodHisNuevo = new NodoArbolHistorial(TipoNodoEnum.NOVALUE, null, -1, -1, "", "NO SE ENCONTRARON REGISTROS");
            treeNodeCreado = new DefaultTreeNode(nodHisNuevo, treeNodeRaiz);
        }

        //RequestContext.getCurrentInstance().update("IdFormHistorias:IdPanelHistorico");
    }

    private boolean estaEnListaTipRegHis(String idBuscado) {
        //buscar si un el tipo de registro se debe o no listar el el arbol del historial
        for (String idTipRegCliSelHis : regCliSelHistorial) {
            if (idBuscado.compareTo(idTipRegCliSelHis) == 0) {
                return true;
            }
        }
        return false;
    }

    public void seleccionaNodoArbol() {//determinar si se activan los botenes de 'editar' y 'PDF' del historial
        btnHistorialDisabled = true;
        btnPdfAgrupadoDisabled = true;
        boolean igualTipoRegistro = true;//determinar si los registros seleccionados son del mismo tipo para activar o no btnPdfAgrupadoDisabled
        int tipoRegistroEncontrado = -1;

        if (treeNodesSeleccionados != null) {
            int contadorRegistrosSeleccionadosHistorial = 0;
            for (TreeNode nodo : treeNodesSeleccionados) {
                NodoArbolHistorial nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
                if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                    if (tipoRegistroEncontrado == -1) {
                        tipoRegistroEncontrado = nodArbolHisSeleccionado.getIdTipoRegistro();
                    } else {
                        if (tipoRegistroEncontrado != nodArbolHisSeleccionado.getIdTipoRegistro()) {
                            igualTipoRegistro = false;
                        }
                    }
                    contadorRegistrosSeleccionadosHistorial++;
                }
            }
            if (contadorRegistrosSeleccionadosHistorial == 0) {//no hay ninguno
                btnHistorialDisabled = true;
                btnPdfAgrupadoDisabled = true;
            } else if (contadorRegistrosSeleccionadosHistorial == 1) {//solo es uno
                btnHistorialDisabled = false;
                btnPdfAgrupadoDisabled = true;
            } else {//hay mas de uno
                if (igualTipoRegistro) {//son del mismo tipo
                    btnHistorialDisabled = true;
                    btnPdfAgrupadoDisabled = false;
                } else {//no son del mismo tipo
                    btnHistorialDisabled = true;
                    btnPdfAgrupadoDisabled = true;
                }
            }
        }
    }

    public void cargarRegistroClinico() {//cargar un registro clinico seleccionado en el arbol de historial de paciente
        if (treeNodesSeleccionados == null) {
            imprimirMensaje("Error", "Se debe seleccionar un registro del histórico", FacesMessage.SEVERITY_ERROR);
            return;
        }
        NodoArbolHistorial nodArbolHisSeleccionado = null;
        for (TreeNode nodo : treeNodesSeleccionados) {
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                break;
            }
        }
        if (nodArbolHisSeleccionado == null) {
            imprimirMensaje("Error", "Se debe seleccionar un registro del histórico", FacesMessage.SEVERITY_ERROR);
            return;
        }

        tipoRegistroClinico = "";
        tipoRegistroClinico = String.valueOf(nodArbolHisSeleccionado.getIdTipoRegistro());//se posicione el combo
        limpiarFormulario();
        //cargo los datos principales a estructuraCampos
        registroEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
        if (registroEncontrado.getIdMedico() != null) {
            idPrestador = registroEncontrado.getIdMedico().getIdUsuario().toString();
            especialidadPrestador = registroEncontrado.getIdMedico().getEspecialidad().getDescripcion();
        } else {
            idPrestador = "";
            especialidadPrestador = "";
        }
        fechaReg = registroEncontrado.getFechaReg();
        fechaSis = registroEncontrado.getFechaSis();

        //cargar los datos adicionales a estructuraCampos
        List<HcDetalle> listaDetalles = registroEncontrado.getHcDetalleList();
        for (HcDetalle detalle : listaDetalles) {
            HcCamposReg campo = camposRegFacade.find(detalle.getHcDetallePK().getIdCampo());
            estructuraCampos.setValor(campo.getPosicion(), detalle.getValor());
        }
        modificandoRegCli = true;
        mostrarFormularioRegistroClinico();
        RequestContext.getCurrentInstance().execute("PF('dlgHistorico').hide();");
        RequestContext.getCurrentInstance().update("IdFormHistorias");
    }

    //---------------------------------------------
    //--------- FUNCIONES REPORTES PDF AGRUPADO------------
    //---------------------------------------------
    public boolean cargarFuenteDeDatosAgrupada() {
        if (treeNodesSeleccionados == null) {//hay nodos seleccionados en el arbol
            imprimirMensaje("Error", "Se debe seleccionar un registro del histórico", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        NodoArbolHistorial nodArbolHisSeleccionado = null;
        for (TreeNode nodo : treeNodesSeleccionados) {
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                break;
            }
        }
        if (nodArbolHisSeleccionado == null) {//de los nodos seleccionados ninguno es regitro clinico
            imprimirMensaje("Error", "Se debe seleccionar un registro del histórico", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
        switch (regEncontrado.getIdTipoReg().getIdTipoReg()) {//DETERMINAR EL TIPO DE REGISTRO AL CUAL SE LE GENERARA EL REPORTE
            case 1://"HISTORIA CLINICA CONSULTA"
                cargarFuenteDatosHistoriaClinicaAgrupada();
                break;
            case 7://"HISTORIA CLINICA PSIQUIATRIA"
                cargarFuenteDatosHistoriaPsiquiatriaAgrupada();
                break;
            case 8://"HISTORIA CLINICA ADULTO MAYOR"
                cargarFuenteDatosHistoriaAdultoAgrupada();
                break;
            case 2://"REFERENCIA - CONTRAREFERENCIA"
                cargarFuenteDatosReferenciaAgrupada();
                break;
            case 5://"NOTA EVOLUCION CONTROL PSIQUIATRIA"
                cargarFuenteDatosNotaPsiquiatriaAgrupada();
                break;
            case 3://"NOTA EVOLUCION CONTROL"
                cargarFuenteDatosNotaControlAgrupada();
                break;
            case 6://"RECETARIO"
                cargarFuenteDatosRecetarioAgrupada();
                break;
        }
        return true;
    }

    private void cargarFuenteDatosHistoriaClinicaAgrupada() {
        NodoArbolHistorial nodArbolHisSeleccionado;
        boolean principalesCargados = false;//saber si los datos principales ya se cargaron o no
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        for (TreeNode nodo : treeNodesSeleccionados) {//RECORRO LOS NODOS SELECCIONADOS Y SI ALGUNO ES DE TIPO: TipoNodoEnum.REGISTRO_HISTORIAL INICIO AGREGACION AL PDF
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
                List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
                //--------------------------CARGA DE DATOS PRINCIPALES SOLO UNA VEZ-------------------------------                
                if (!principalesCargados) {//cargarDatosPrincipales
                    principalesCargados = true;
                    //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
                    datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
                    datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
                    if (regEncontrado.getIdMedico() != null) {
                        datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
                        if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
                        }
                        datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

                        if (regEncontrado.getIdMedico().getFirma() != null) {
                            datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
                        }
                    }
                    datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
                    datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
                    if (pacienteSeleccionado.getTipoIdentificacion() != null) {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
                    } else {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
                    }
                    if (pacienteSeleccionado.getFechaNacimiento() != null) {
                        datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
                        datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
                    } else {
                        datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
                        datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
                    }
                    strAux = "No refiere";
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
                    }
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
                    }
                    datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
                    //datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
                    if (pacienteSeleccionado.getIdAdministradora() != null) {
                        datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
                    } else {
                        datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getEstadoCivil() != null) {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
                    } else {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getOcupacion() != null) {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
                    } else {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
                    }
                    datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
                    datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
                    if (pacienteSeleccionado.getGenero() != null) {
                        datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
                    } else {
                        datosPrincipales.setGenero("<b>GENERO: </b> ");
                    }
                    //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
                    datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
                    if (regEncontrado.getIdTipoReg().getIdTipoReg() != 8) {
                        datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
                        datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
                    }
                    datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
                    datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
                    datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
                }
                //--------------------------CARGA DE DATOS ADICIONALES POR CADA REGISTRO ENCONTRADO---------------

                strAux = "<b>FECHA:</b> " + sdfDateHour.format(regEncontrado.getFechaReg()) + "<br/>";
                //CARGAR DATOS ADICIONALES POR BUSQUEDA (hc_detalle)---------------------------------------------------                        
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("MOTIVO DE CONSULTA", "motivo_consulta", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ENFERMEDAD ACTUAL", "enf_actual", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("HISTORIA Y ANTECEDENTES FAMILIARES", "his_ant_fliares", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DESARROLLO PRENATAL", "des_prenatal", listaDetalles)) + "<br/>";
                //DESARROLLO POSTNATAL(COMPUESTA)                                    
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DESARROLLO POSTNATAL MOTOR", "des_pos_motor", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DESARROLLO POSTNATAL LENGUAGE", "des_pos_lenguaje", listaDetalles)) + "<br/>";
                //ESCOLARIDAD(COMPUESTA)                        
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD: INICIO Y ADAPTACION", "esc_ini_adap", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD: SITUACION ESCOLAR ACTUAL", "esc_situa_actual", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD: CONDUCTA SOCIAL Y EMOCIONAL", "esc_cond_soc_emoc", listaDetalles)) + "<br/>";
                //ANTECEDENTES(COMPUESTA)
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES MEDICOS", "ant_medicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES QUIRURGICOS", "ant_quirurjico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TRANSFUNCIONALES", "ant_transfuncionales", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES INMUNOLOGICOS", "ant_inmunologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES ALERGICOS", "ant_alergicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TRAUMATICOS", "ant_traumaticos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES PSICOLOGICOS", "ant_psicologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES FARMACOLOGICOS", "ant_farmacologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES FAMILIARES", "ant_familiares", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TOXICOS", "ant_toxicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES OTROS", "ant_otros", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("REVISION POR SISTEMA", "revision_sistema", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANALISIS", "analisis", listaDetalles)) + "<br/>";
                //IMPRESION DIAGNOSTICA(COMPUESTA)            
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO PPAL", "imp_diag_ppal", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 1", "imp_diag_1", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 2", "imp_diag_2", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 3", "imp_diag_3", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("PLAN DE TRATAMIENTO", "plan_trat", listaDetalles)) + "<br/>";
                if (regEncontrado.getIdMedico() != null) {
                    String especialidad = "-";
                    String registroProfesional = "-";
                    if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                        especialidad = regEncontrado.getIdMedico().getEspecialidad().getDescripcion();
                    }
                    if (regEncontrado.getIdMedico().getRegistroProfesional() != null) {
                        registroProfesional = regEncontrado.getIdMedico().getRegistroProfesional();
                    }
                    strAux = strAux + "<b> PROFESIONAL: </b>" + regEncontrado.getIdMedico().nombreCompleto() + ", "
                            + especialidad + " <b>REG. MEDICO: </b>" + registroProfesional;
                } else {
                    strAux = strAux + "<b> PROFESIONAL: </b> No refiere";
                }
                listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("", strAux));
            }
            datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        }
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosHistoriaPsiquiatriaAgrupada() {
        NodoArbolHistorial nodArbolHisSeleccionado;
        boolean principalesCargados = false;//saber si los datos principales ya se cargaron o no
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;

        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        for (TreeNode nodo : treeNodesSeleccionados) {//RECORRO LOS NODOS SELECCIONADOS Y SI ALGUNO ES DE TIPO: TipoNodoEnum.REGISTRO_HISTORIAL INICIO AGREGACION AL PDF
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
                List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
                //--------------------------CARGA DE DATOS PRINCIPALES SOLO UNA VEZ-------------------------------
                if (!principalesCargados) {//cargarDatosPrincipales
                    principalesCargados = true;
                    //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
                    datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
                    datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
                    if (regEncontrado.getIdMedico() != null) {
                        datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
                        if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
                        }
                        datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

                        if (regEncontrado.getIdMedico().getFirma() != null) {
                            datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
                        }
                    }
                    datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
                    datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
                    if (pacienteSeleccionado.getTipoIdentificacion() != null) {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
                    } else {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
                    }
                    if (pacienteSeleccionado.getFechaNacimiento() != null) {
                        datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
                        datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
                    } else {
                        datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
                        datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
                    }
                    strAux = "No refiere";
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
                    }
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
                    }
                    datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
                    //datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
                    if (pacienteSeleccionado.getIdAdministradora() != null) {
                        datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
                    } else {
                        datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getEstadoCivil() != null) {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
                    } else {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getOcupacion() != null) {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
                    } else {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
                    }
                    datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
                    datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
                    if (pacienteSeleccionado.getGenero() != null) {
                        datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
                    } else {
                        datosPrincipales.setGenero("<b>GENERO: </b> ");
                    }
                    //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
                    datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
                    if (regEncontrado.getIdTipoReg().getIdTipoReg() != 8) {
                        datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
                        datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
                    }
                    datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
                    datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
                    datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
                }
                //------------------------------------------------------------------------------------------------
                //--------------------------CARGA DE DATOS ADICIONALES POR CADA REGISTRO ENCONTRADO---------------
                //------------------------------------------------------------------------------------------------

                strAux = "<b>FECHA: </b>" + sdfDateHour.format(regEncontrado.getFechaReg()) + "<br/>";
                //CARGAR DATOS ADICIONALES POR BUSQUEDA (hc_detalle)---------------------------------------------------                        
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("MOTIVO DE CONSULTA", "motivo_consulta", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ENFERMEDAD ACTUAL", "enf_actual", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("HISTORIA Y ANTECEDENTES FAMILIARES", "his_ant_fliares", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DESARROLLO PRENATAL", "des_prenatal", listaDetalles)) + "<br/>";
                //DESARROLLO POSTNATAL(COMPUESTA)                                    
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DESARROLLO POSTNATAL MOTOR", "des_pos_motor", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DESARROLLO POSTNATAL LENGUAGE", "des_pos_lenguaje", listaDetalles)) + "<br/>";
                //ESCOLARIDAD(COMPUESTA)                        
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD: INICIO Y ADAPTACION", "esc_ini_adap", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD: SITUACION ESCOLAR ACTUAL", "esc_situa_actual", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD: CONDUCTA SOCIAL Y EMOCIONAL", "esc_cond_soc_emoc", listaDetalles)) + "<br/>";
                //ANTECEDENTES(COMPUESTA)
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES MEDICOS", "ant_medicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES QUIRURGICOS", "ant_quirurjico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TRANSFUNCIONALES", "ant_transfuncionales", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES INMUNOLOGICOS", "ant_inmunologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES ALERGICOS", "ant_alergicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TRAUMATICOS", "ant_traumaticos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES PSICOLOGICOS", "ant_psicologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES FARMACOLOGICOS", "ant_farmacologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES FAMILIARES", "ant_familiares", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TOXICOS", "ant_toxicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES OTROS", "ant_otros", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("REVISION POR SISTEMA", "revision_sistema", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANALISIS", "analisis", listaDetalles)) + "<br/>";
                //IMPRESION DIAGNOSTICA(COMPUESTA)            
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO PPAL", "imp_diag_ppal", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 1", "imp_diag_1", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 2", "imp_diag_2", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 3", "imp_diag_3", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("PLAN DE TRATAMIENTO", "plan_trat", listaDetalles)) + "<br/>";
                if (regEncontrado.getIdMedico() != null) {
                    String especialidad = "-";
                    String registroProfesional = "-";
                    if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                        especialidad = regEncontrado.getIdMedico().getEspecialidad().getDescripcion();
                    }
                    if (regEncontrado.getIdMedico().getRegistroProfesional() != null) {
                        registroProfesional = regEncontrado.getIdMedico().getRegistroProfesional();
                    }
                    strAux = strAux + "<b> PROFESIONAL: </b>" + regEncontrado.getIdMedico().nombreCompleto() + ", " + especialidad + " <b>REG. MEDICO: </b>" + registroProfesional;
                } else {
                    strAux = strAux + "<b> PROFESIONAL: </b> No refiere";
                }
                listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("", strAux));
            }
            datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        }
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosHistoriaAdultoAgrupada() {
        NodoArbolHistorial nodArbolHisSeleccionado;
        boolean principalesCargados = false;//saber si los datos principales ya se cargaron o no
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        for (TreeNode nodo : treeNodesSeleccionados) {//RECORRO LOS NODOS SELECCIONADOS Y SI ALGUNO ES DE TIPO: TipoNodoEnum.REGISTRO_HISTORIAL INICIO AGREGACION AL PDF
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
                List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
                //--------------------------CARGA DE DATOS PRINCIPALES SOLO UNA VEZ-------------------------------
                if (!principalesCargados) {//cargarDatosPrincipales
                    principalesCargados = true;
                    //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
                    datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
                    datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
                    if (regEncontrado.getIdMedico() != null) {
                        datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
                        if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
                        }
                        datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

                        if (regEncontrado.getIdMedico().getFirma() != null) {
                            datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
                        }
                    }
                    datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
                    datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
                    if (pacienteSeleccionado.getTipoIdentificacion() != null) {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
                    } else {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
                    }
                    if (pacienteSeleccionado.getFechaNacimiento() != null) {
                        datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
                        datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
                    } else {
                        datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
                        datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
                    }
                    strAux = "No refiere";
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
                    }
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
                    }
                    datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
                    //datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
                    if (pacienteSeleccionado.getIdAdministradora() != null) {
                        datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
                    } else {
                        datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getEstadoCivil() != null) {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
                    } else {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getOcupacion() != null) {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
                    } else {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
                    }
                    datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
                    datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
                    if (pacienteSeleccionado.getGenero() != null) {
                        datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
                    } else {
                        datosPrincipales.setGenero("<b>GENERO: </b> ");
                    }
                    //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
                    datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
                    if (regEncontrado.getIdTipoReg().getIdTipoReg() != 8) {
                        datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
                        datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
                    }
                    datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
                    datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
                    datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
                }
                //--------------------------CARGA DE DATOS ADICIONALES POR CADA REGISTRO ENCONTRADO---------------
                strAux = "<b> FECHA: </b>" + sdfDateHour.format(regEncontrado.getFechaReg()) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("MOTIVO DE CONSULTA", "motivo_consulta", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ENFERMEDAD ACTUAL", "enf_actual", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("HISTORIA Y ANTECEDENTES FAMILIARES", "his_ant_fliares", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("FUNCIONES MOTORAS", "fun_motoras", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("FUNCIONES DEL LENGUAJE", "fun_lenguaje", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("CONDUCTA SOCIAL Y EMOCIONAL", "cond_soc_emoc", listaDetalles)) + "<br/>";
                //ANTECEDENTES(COMPUESTA)
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES MEDICOS", "ant_medicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES QUIRURGICOS", "ant_quirurjico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TRANSFUNCIONALES", "ant_transfuncionales", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES INMUNOLOGICOS", "ant_inmunologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES ALERGICOS", "ant_alergicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TRAUMATICOS", "ant_traumaticos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES PSICOLOGICOS", "ant_psicologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES FARMACOLOGICOS", "ant_farmacologico", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES FAMILIARES", "ant_familiares", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES TOXICOS", "ant_toxicos", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANTECEDENTES OTROS", "ant_otros", listaDetalles)) + "<br/>";

                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("REVISION POR SISTEMA", "revision_sistema", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANALISIS (CONDUCTA DEL SUEÑO, ALIMENTACION, CONTROL ESFINTE)", "analisis", listaDetalles)) + "<br/>";
                //IMPRESION DIAGNOSTICA(COMPUESTA)            
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO PPAL", "imp_diag_ppal", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 1", "imp_diag_1", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 2", "imp_diag_2", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 3", "imp_diag_3", listaDetalles)) + "<br/>";

                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("PLAN DE TRATAMIENTO", "plan_trat", listaDetalles)) + "<br/>";
                if (regEncontrado.getIdMedico() != null) {
                    String especialidad = "-";
                    String registroProfesional = "-";
                    if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                        especialidad = regEncontrado.getIdMedico().getEspecialidad().getDescripcion();
                    }
                    if (regEncontrado.getIdMedico().getRegistroProfesional() != null) {
                        registroProfesional = regEncontrado.getIdMedico().getRegistroProfesional();
                    }
                    strAux = strAux + "<b> PROFESIONAL: </b>" + regEncontrado.getIdMedico().nombreCompleto() + ", " + especialidad + " <b>REG. MEDICO: </b>" + registroProfesional;
                } else {
                    strAux = strAux + "<b> PROFESIONAL: </b> No refiere";
                }
                listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("", strAux));
            }
            datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        }
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosReferenciaAgrupada() {
        NodoArbolHistorial nodArbolHisSeleccionado;
        boolean principalesCargados = false;//saber si los datos principales ya se cargaron o no
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;

        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        for (TreeNode nodo : treeNodesSeleccionados) {//RECORRO LOS NODOS SELECCIONADOS Y SI ALGUNO ES DE TIPO: TipoNodoEnum.REGISTRO_HISTORIAL INICIO AGREGACION AL PDF
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
                List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
                //--------------------------CARGA DE DATOS PRINCIPALES SOLO UNA VEZ-------------------------------
                if (!principalesCargados) {//cargarDatosPrincipales
                    principalesCargados = true;
                    //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
                    datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
                    datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
                    if (regEncontrado.getIdMedico() != null) {
                        datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
                        if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
                        }
                        datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

                        if (regEncontrado.getIdMedico().getFirma() != null) {
                            datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
                        }
                    }
                    datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
                    datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
                    if (pacienteSeleccionado.getTipoIdentificacion() != null) {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
                    } else {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
                    }
                    if (pacienteSeleccionado.getFechaNacimiento() != null) {
                        datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
                        datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
                    } else {
                        datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
                        datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
                    }
                    strAux = "No refiere";
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
                    }
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
                    }
                    datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
                    //datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
                    if (pacienteSeleccionado.getIdAdministradora() != null) {
                        datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
                    } else {
                        datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getEstadoCivil() != null) {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
                    } else {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getOcupacion() != null) {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
                    } else {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
                    }
                    datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
                    datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
                    if (pacienteSeleccionado.getGenero() != null) {
                        datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
                    } else {
                        datosPrincipales.setGenero("<b>GENERO: </b> ");
                    }
                    //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
                    //datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
                    //datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
                    //datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));

                    //datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
                    //datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
                    datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
                    datosPrincipales.setFechaNacimiento(datosPrincipales.getGenero());
                    datosPrincipales.setLugarNacimiento(datosPrincipales.getTel());
                    datosPrincipales.setEps(getHtmlDesdeDatoAdicional(buscarDetalle("INSTITUCION DE ORIGEN", "ins_origen", listaDetalles)));
                    datosPrincipales.setGenero(getHtmlDesdeDatoAdicional(buscarDetalle("PROFESIONAL SOLICITANTE", "prof_solic", listaDetalles)));
                    datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("ESPECIALIDAD SOLICITADA", "esp_sol", listaDetalles)));
                    datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("REFERENCIA O CONTRAREFERENCIA", "tip_ref", listaDetalles)));
                    datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("DX CIE 10", "diag_ppal", listaDetalles)));
                    datosPrincipales.setEstadoCivil("");
                    datosPrincipales.setTel("");
                    datosPrincipales.setOcupacion("");
                    datosPrincipales.setDireccion("");
                    datosPrincipales.setNumeroAutorizacion("");
                }
                //--------------------------CARGA DE DATOS ADICIONALES POR CADA REGISTRO ENCONTRADO---------------
                strAux = "<b> FECHA: </b>" + sdfDateHour.format(regEncontrado.getFechaReg()) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO", "diag_text", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("RESUMEN COMPLETO HISTORIA CLINICA", "res_hc", listaDetalles)) + "<br/>";
                if (regEncontrado.getIdMedico() != null) {
                    String especialidad = "-";
                    String registroProfesional = "-";
                    if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                        especialidad = regEncontrado.getIdMedico().getEspecialidad().getDescripcion();
                    }
                    if (regEncontrado.getIdMedico().getRegistroProfesional() != null) {
                        registroProfesional = regEncontrado.getIdMedico().getRegistroProfesional();
                    }
                    strAux = strAux + "<b> PROFESIONAL: </b>" + regEncontrado.getIdMedico().nombreCompleto() + ", " + especialidad + " <b>REG. MEDICO: </b>" + registroProfesional;
                } else {
                    strAux = strAux + "<b> PROFESIONAL: </b> No refiere";
                }
                listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("", strAux));
            }
            datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        }
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosNotaPsiquiatriaAgrupada() {
        NodoArbolHistorial nodArbolHisSeleccionado;
        boolean principalesCargados = false;//saber si los datos principales ya se cargaron o no
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;

        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        for (TreeNode nodo : treeNodesSeleccionados) {//RECORRO LOS NODOS SELECCIONADOS Y SI ALGUNO ES DE TIPO: TipoNodoEnum.REGISTRO_HISTORIAL INICIO AGREGACION AL PDF
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
                List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
                //--------------------------CARGA DE DATOS PRINCIPALES SOLO UNA VEZ-------------------------------
                if (!principalesCargados) {//cargarDatosPrincipales
                    principalesCargados = true;
                    //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
                    datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
                    datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
                    if (regEncontrado.getIdMedico() != null) {
                        datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
                        if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
                        }
                        datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

                        if (regEncontrado.getIdMedico().getFirma() != null) {
                            datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
                        }
                    }
                    datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
                    datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
                    if (pacienteSeleccionado.getTipoIdentificacion() != null) {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
                    } else {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
                    }
                    if (pacienteSeleccionado.getFechaNacimiento() != null) {
                        datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
                        datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
                    } else {
                        datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
                        datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
                    }
                    strAux = "No refiere";
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
                    }
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
                    }
                    datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
                    //datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
                    if (pacienteSeleccionado.getIdAdministradora() != null) {
                        datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
                    } else {
                        datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getEstadoCivil() != null) {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
                    } else {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getOcupacion() != null) {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
                    } else {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
                    }
                    datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
                    datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
                    if (pacienteSeleccionado.getGenero() != null) {
                        datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
                    } else {
                        datosPrincipales.setGenero("<b>GENERO: </b> ");
                    }
                    //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
                    //datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
                    //datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
                    //datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
                    datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "servicio", listaDetalles)));
                    datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "autorizacion", listaDetalles)));
                    datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
                }
                //--------------------------CARGA DE DATOS ADICIONALES POR CADA REGISTRO ENCONTRADO---------------
                strAux = "<b> FECHA: </b>" + sdfDateHour.format(regEncontrado.getFechaReg()) + "<br/>";
                datosPrincipales.setColegio(datosPrincipales.getDireccion());
                datosPrincipales.setEscolaridad(datosPrincipales.getTel());
                if (pacienteSeleccionado.getAcompanante() != null) {
                    datosPrincipales.setEstadoCivil("<b>ACOMPAÑANTE O ACUDIENTE</b>: " + pacienteSeleccionado.getAcompanante());//ACOMPAÑANTE O ACUDIENTE
                } else {
                    datosPrincipales.setEstadoCivil("<b>ACOMPAÑANTE O ACUDIENTE</b>: ");
                }
                if (pacienteSeleccionado.getParentesco() != null) {
                    datosPrincipales.setOcupacion("<b>PARENTEZCO</b>: " + pacienteSeleccionado.getParentesco().getDescripcion());//PARENTEZCO
                } else {
                    datosPrincipales.setOcupacion("<b>PARENTEZCO</b>: ");//PARENTEZCO
                }
                datosPrincipales.setDireccion(
                        "<b>NUMERO DE SESION: </b>"
                        + buscarDetalle("NUM SESION", "num_sesion", listaDetalles).getContenido()
                        + " <b>DE: </b> "
                        + buscarDetalle("NUM SESION", "de", listaDetalles).getContenido());//SESION TAL DE TAL
                datosPrincipales.setTel("");
                datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "servicio", listaDetalles)));

                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DETALLE CONSULTA", "nota", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ANALISIS", "analisis", listaDetalles)) + "<br/>";
                //IMPRESION DIAGNOSTICA(COMPUESTA)            
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO PPAL", "imp_diag_ppal", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 1", "imp_diag_1", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 2", "imp_diag_2", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 3", "imp_diag_3", listaDetalles)) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("PLAN DE TRATAMIENTO", "plan_trat", listaDetalles)) + "<br/>";
                if (regEncontrado.getIdMedico() != null) {
                    String especialidad = "-";
                    String registroProfesional = "-";
                    if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                        especialidad = regEncontrado.getIdMedico().getEspecialidad().getDescripcion();
                    }
                    if (regEncontrado.getIdMedico().getRegistroProfesional() != null) {
                        registroProfesional = regEncontrado.getIdMedico().getRegistroProfesional();
                    }
                    strAux = strAux + "<b> PROFESIONAL: </b>" + regEncontrado.getIdMedico().nombreCompleto() + ", " + especialidad + " <b>REG. MEDICO: </b>" + registroProfesional;
                } else {
                    strAux = strAux + "<b> PROFESIONAL: </b> No refiere";
                }
                listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("", strAux));
            }
            datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        }
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosNotaControlAgrupada() {
        NodoArbolHistorial nodArbolHisSeleccionado;
        boolean principalesCargados = false;//saber si los datos principales ya se cargaron o no
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;

        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        for (TreeNode nodo : treeNodesSeleccionados) {//RECORRO LOS NODOS SELECCIONADOS Y SI ALGUNO ES DE TIPO: TipoNodoEnum.REGISTRO_HISTORIAL INICIO AGREGACION AL PDF
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
                List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
                //--------------------------CARGA DE DATOS PRINCIPALES SOLO UNA VEZ-------------------------------
                if (!principalesCargados) {//cargarDatosPrincipales
                    principalesCargados = true;
                    //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
                    datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
                    datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
                    if (regEncontrado.getIdMedico() != null) {
                        datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
                        if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
                        }
                        datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

                        if (regEncontrado.getIdMedico().getFirma() != null) {
                            datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
                        }
                    }
                    datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
                    datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
                    if (pacienteSeleccionado.getTipoIdentificacion() != null) {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
                    } else {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
                    }
                    if (pacienteSeleccionado.getFechaNacimiento() != null) {
                        datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
                        datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
                    } else {
                        datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
                        datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
                    }
                    strAux = "No refiere";
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
                    }
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
                    }
                    datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
                    //REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
                    if (pacienteSeleccionado.getIdAdministradora() != null) {
                        datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
                    } else {
                        datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getEstadoCivil() != null) {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
                    } else {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getOcupacion() != null) {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
                    } else {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
                    }
                    datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
                    datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
                    if (pacienteSeleccionado.getGenero() != null) {
                        datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
                    } else {
                        datosPrincipales.setGenero("<b>GENERO: </b> ");
                    }
                    //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
                    //datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
                    //datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
                    //datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
                    datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "servicio", listaDetalles)));
                    datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "autorizacion", listaDetalles)));
                    datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
                }
                //--------------------------CARGA DE DATOS ADICIONALES POR CADA REGISTRO ENCONTRADO---------------
                strAux = "<b> FECHA: </b>" + sdfDateHour.format(regEncontrado.getFechaReg()) + "<br/>";
                datosPrincipales.setColegio(datosPrincipales.getDireccion());
                datosPrincipales.setEscolaridad(datosPrincipales.getTel());
                if (pacienteSeleccionado.getAcompanante() != null) {
                    datosPrincipales.setEstadoCivil("<b>ACOMPAÑANTE O ACUDIENTE</b>: " + pacienteSeleccionado.getAcompanante());//ACOMPAÑANTE O ACUDIENTE
                } else {
                    datosPrincipales.setEstadoCivil("<b>ACOMPAÑANTE O ACUDIENTE</b>: ");
                }
                if (pacienteSeleccionado.getParentesco() != null) {
                    datosPrincipales.setOcupacion("<b>PARENTEZCO</b>: " + pacienteSeleccionado.getParentesco().getDescripcion());//PARENTEZCO
                } else {
                    datosPrincipales.setOcupacion("<b>PARENTEZCO</b>: ");//PARENTEZCO
                }
                datosPrincipales.setDireccion(
                        "<b>NUMERO DE SESION: </b>"
                        + buscarDetalle("NUM SESION", "num_sesion", listaDetalles).getContenido()
                        + " <b>DE: </b> "
                        + buscarDetalle("NUM SESION", "de", listaDetalles).getContenido());//SESION TAL DE TAL
                datosPrincipales.setTel("");
                datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "servicio", listaDetalles)));
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("EVOLUCION / NOTA", "nota", listaDetalles)) + "<br/>";
                if (regEncontrado.getIdMedico() != null) {
                    String especialidad = "-";
                    String registroProfesional = "-";
                    if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                        especialidad = regEncontrado.getIdMedico().getEspecialidad().getDescripcion();
                    }
                    if (regEncontrado.getIdMedico().getRegistroProfesional() != null) {
                        registroProfesional = regEncontrado.getIdMedico().getRegistroProfesional();
                    }
                    strAux = strAux + "<b> PROFESIONAL: </b>" + regEncontrado.getIdMedico().nombreCompleto() + ", " + especialidad + " <b>REG. MEDICO: </b>" + registroProfesional;
                } else {
                    strAux = strAux + "<b> PROFESIONAL: </b> No refiere";
                }
                listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("", strAux));
            }
            datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        }
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosRecetarioAgrupada() {
        NodoArbolHistorial nodArbolHisSeleccionado;
        boolean principalesCargados = false;//saber si los datos principales ya se cargaron o no
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;

        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        for (TreeNode nodo : treeNodesSeleccionados) {//RECORRO LOS NODOS SELECCIONADOS Y SI ALGUNO ES DE TIPO: TipoNodoEnum.REGISTRO_HISTORIAL INICIO AGREGACION AL PDF
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
                List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
                //--------------------------CARGA DE DATOS PRINCIPALES SOLO UNA VEZ-------------------------------
                if (!principalesCargados) {//cargarDatosPrincipales
                    principalesCargados = true;
                    //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
                    datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
                    datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
                    if (regEncontrado.getIdMedico() != null) {
                        datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
                        if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
                        }
                        datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

                        if (regEncontrado.getIdMedico().getFirma() != null) {
                            datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
                        }
                    }

                    datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
                    datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
                    if (pacienteSeleccionado.getTipoIdentificacion() != null) {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
                    } else {
                        datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
                    }
                    if (pacienteSeleccionado.getFechaNacimiento() != null) {
                        datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
                        datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
                    } else {
                        datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
                        datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
                    }
                    strAux = "No refiere";
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
                    }
                    if (pacienteSeleccionado.getMunNacimiento() != null) {
                        strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
                    }
                    datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
                    //datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
                    if (pacienteSeleccionado.getIdAdministradora() != null) {
                        datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
                    } else {
                        datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getEstadoCivil() != null) {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
                    } else {
                        datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
                    }
                    if (pacienteSeleccionado.getOcupacion() != null) {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
                    } else {
                        datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
                    }
                    datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
                    datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
                    if (pacienteSeleccionado.getGenero() != null) {
                        datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
                    } else {
                        datosPrincipales.setGenero("<b>GENERO: </b> ");
                    }
                    //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
                    //datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));                    
                    //datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
                    //datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));                    
                    //datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
                    //datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
                    datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
                }
                //--------------------------CARGA DE DATOS ADICIONALES POR CADA REGISTRO ENCONTRADO---------------
                strAux = "<b> FECHA: </b>" + sdfDateHour.format(regEncontrado.getFechaReg()) + "<br/>";
                strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DETALLES RECETA", "recetario_txt", listaDetalles)) + "<br/>";
                if (regEncontrado.getIdMedico() != null) {
                    String especialidad = "-";
                    String registroProfesional = "-";
                    if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                        especialidad = regEncontrado.getIdMedico().getEspecialidad().getDescripcion();
                    }
                    if (regEncontrado.getIdMedico().getRegistroProfesional() != null) {
                        registroProfesional = regEncontrado.getIdMedico().getRegistroProfesional();
                    }
                    strAux = strAux + "<b> PROFESIONAL: </b>" + regEncontrado.getIdMedico().nombreCompleto() + ", " + especialidad + " <b>REG. MEDICO: </b>" + registroProfesional;
                } else {
                    strAux = strAux + "<b> PROFESIONAL: </b> No refiere";
                }
                listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("", strAux));
            }
            datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        }
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    //---------------------------------------------
    //--------- FUNCIONES REPORTES PDF UNICO ------
    //---------------------------------------------
    public boolean cargarFuenteDeDatos() {//carga los fuente de datos que recibe el pdf 'JRBeanCollectionDataSource(listaDatosPrincipales)'
        if (treeNodesSeleccionados == null) {//hay nodos seleccionados en el arbol
            imprimirMensaje("Error", "Se debe seleccionar un registro del histórico", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        NodoArbolHistorial nodArbolHisSeleccionado = null;
        for (TreeNode nodo : treeNodesSeleccionados) {
            nodArbolHisSeleccionado = (NodoArbolHistorial) nodo.getData();
            if (nodArbolHisSeleccionado.getTipoDeNodo() == TipoNodoEnum.REGISTRO_HISTORIAL) {
                break;
            }
        }
        if (nodArbolHisSeleccionado == null) {//de los nodos seleccionados ninguno es regitro clinico
            imprimirMensaje("Error", "Se debe seleccionar un registro del histórico", FacesMessage.SEVERITY_ERROR);
            return false;
        }
        HcRegistro regEncontrado = registroFacade.find(nodArbolHisSeleccionado.getIdRegistro());
        switch (regEncontrado.getIdTipoReg().getIdTipoReg()) {//DETERMINAR EL TIPO DE REGISTRO AL CUAL SE LE GENERARA EL REPORTE
            case 1://"HISTORIA CLINICA CONSULTA"
                cargarFuenteDatosHistoriaClinica(regEncontrado);
                break;
            case 7://"HISTORIA CLINICA PSIQUIATRIA"
                cargarFuenteDatosHistoriaPsiquiatria(regEncontrado);
                break;
            case 8://"HISTORIA CLINICA ADULTO MAYOR"
                cargarFuenteDatosHistoriaAdulto(regEncontrado);
                break;
            case 2://"REFERENCIA - CONTRAREFERENCIA"
                cargarFuenteDatosReferencia(regEncontrado);
                break;
            case 5://"NOTA EVOLUCION CONTROL PSIQUIATRIA"
                cargarFuenteDatosNotaPsiquiatria(regEncontrado);
                break;
            case 3://"NOTA EVOLUCION CONTROL"
                cargarFuenteDatosNotaControl(regEncontrado);
                break;
            case 6://"RECETARIO"
                cargarFuenteDatosRecetario(regEncontrado);
                break;
        }
        return true;
    }

    private void cargarFuenteDatosHistoriaClinica(HcRegistro regEncontrado) {
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
        datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
        datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
        datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
        datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
        if (regEncontrado.getIdMedico() != null) {
            datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
            if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
            }
            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

            if (regEncontrado.getIdMedico().getFirma() != null) {
                datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
            }
        }
        if (pacienteSeleccionado.getTipoIdentificacion() != null) {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
        } else {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
        }
        if (pacienteSeleccionado.getFechaNacimiento() != null) {
            datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
            datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
        } else {
            datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
            datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
        }
        strAux = "No refiere";
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
        }
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
        }
        datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
        datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
        if (pacienteSeleccionado.getIdAdministradora() != null) {
            datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
        } else {
            datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getEstadoCivil() != null) {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
        } else {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getOcupacion() != null) {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
        } else {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
        }
        datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
        datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
        if (pacienteSeleccionado.getGenero() != null) {
            datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
        } else {
            datosPrincipales.setGenero("<b>GENERO: </b> ");
        }
        //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
        datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
        datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
        datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
        datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
        datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
        datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
        //CARGAR DATOS ADICIONALES POR BUSQUEDA (hc_detalle)---------------------------------------------------                
        listadoDatosAdicionales.add(buscarDetalle("MOTIVO DE CONSULTA", "motivo_consulta", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("ENFERMEDAD ACTUAL", "enf_actual", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("HISTORIA Y ANTECEDENTES FAMILIARES", "his_ant_fliares", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("DESARROLLO PRENATAL", "des_prenatal", listaDetalles));
        //DESARROLLO POSTNATAL(COMPUESTA)            
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("MOTOR", "des_pos_motor", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("LENGUAGE", "des_pos_lenguaje", listaDetalles)) + "<br/>";
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("DESARROLLO POSTNATAL", strAux));
        //ESCOLARIDAD(COMPUESTA)
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("INICIO Y ADAPTACION", "esc_ini_adap", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("SITUACION ESCOLAR ACTUAL", "esc_situa_actual", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("CONDUCTA SOCIAL Y EMOCIONAL", "esc_cond_soc_emoc", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("ESCOLARIDAD", strAux));
        //ANTECEDENTES(COMPUESTA)
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("MEDICOS", "ant_medicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("QUIRURGICOS", "ant_quirurjico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TRANSFUNCIONALES", "ant_transfuncionales", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("INMUNOLOGICOS", "ant_inmunologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ALERGICOS", "ant_alergicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TRAUMATICOS", "ant_traumaticos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("PSICOLOGICOS", "ant_psicologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("FARMACOLOGICOS", "ant_farmacologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("FAMILIARES", "ant_familiares", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TOXICOS", "ant_toxicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("OTROS", "ant_otros", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("ANTECEDENTES", strAux));
        listadoDatosAdicionales.add(buscarDetalle("REVISION POR SISTEMA", "revision_sistema", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("ANALISIS", "analisis", listaDetalles));
        //IMPRESION DIAGNOSTICA(COMPUESTA)            
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO PPAL", "imp_diag_ppal", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 1", "imp_diag_1", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 2", "imp_diag_2", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 3", "imp_diag_3", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("IMPRESION DIAGNOSTICA", strAux));
        listadoDatosAdicionales.add(buscarDetalle("PLAN DE TRATAMIENTO", "plan_trat", listaDetalles));
        datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosHistoriaPsiquiatria(HcRegistro regEncontrado) {
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
        datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
        datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
        if (regEncontrado.getIdMedico() != null) {
            datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
            if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
            }
            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

            if (regEncontrado.getIdMedico().getFirma() != null) {
                datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
            }
        }
        datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
        datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
        if (pacienteSeleccionado.getTipoIdentificacion() != null) {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
        } else {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
        }
        if (pacienteSeleccionado.getFechaNacimiento() != null) {
            datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
            datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
        } else {
            datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
            datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
        }
        strAux = "No refiere";
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
        }
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
        }
        datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
        datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
        if (pacienteSeleccionado.getIdAdministradora() != null) {
            datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
        } else {
            datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getEstadoCivil() != null) {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
        } else {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getOcupacion() != null) {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
        } else {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
        }
        datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
        datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
        if (pacienteSeleccionado.getGenero() != null) {
            datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
        } else {
            datosPrincipales.setGenero("<b>GENERO: </b> ");
        }
        //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
        datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
        datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
        datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
        datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
        datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
        datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
        //CARGAR DATOS ADICIONALES POR BUSQUEDA (hc_detalle)---------------------------------------------------                
        listadoDatosAdicionales.add(buscarDetalle("MOTIVO DE CONSULTA", "motivo_consulta", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("ENFERMEDAD ACTUAL", "enf_actual", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("HISTORIA Y ANTECEDENTES FAMILIARES", "his_ant_fliares", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("DESARROLLO PRENATAL", "des_prenatal", listaDetalles));
        //DESARROLLO POSTNATAL(COMPUESTA)            
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("MOTOR", "des_pos_motor", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("LENGUAGE", "des_pos_lenguaje", listaDetalles)) + "<br/>";
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("DESARROLLO POSTNATAL", strAux));
        //ESCOLARIDAD(COMPUESTA)
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("INICIO Y ADAPTACION", "esc_ini_adap", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("SITUACION ESCOLAR ACTUAL", "esc_situa_actual", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("CONDUCTA SOCIAL Y EMOCIONAL", "esc_cond_soc_emoc", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("ESCOLARIDAD", strAux));
        //ANTECEDENTES(COMPUESTA)
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("MEDICOS", "ant_medicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("QUIRURGICOS", "ant_quirurjico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TRANSFUNCIONALES", "ant_transfuncionales", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("INMUNOLOGICOS", "ant_inmunologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ALERGICOS", "ant_alergicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TRAUMATICOS", "ant_traumaticos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("PSICOLOGICOS", "ant_psicologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("FARMACOLOGICOS", "ant_farmacologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("FAMILIARES", "ant_familiares", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TOXICOS", "ant_toxicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("OTROS", "ant_otros", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("ANTECEDENTES", strAux));
        listadoDatosAdicionales.add(buscarDetalle("REVISION POR SISTEMA", "revision_sistema", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("ANALISIS", "analisis", listaDetalles));
        //IMPRESION DIAGNOSTICA(COMPUESTA)            
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO PPAL", "imp_diag_ppal", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 1", "imp_diag_1", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 2", "imp_diag_2", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 3", "imp_diag_3", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("IMPRESION DIAGNOSTICA", strAux));
        listadoDatosAdicionales.add(buscarDetalle("PLAN DE TRATAMIENTO", "plan_trat", listaDetalles));
        datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosHistoriaAdulto(HcRegistro regEncontrado) {
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
        datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
        datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
        if (regEncontrado.getIdMedico() != null) {
            datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
            if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
            }
            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

            if (regEncontrado.getIdMedico().getFirma() != null) {
                datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
            }
        }
        datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
        datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
        if (pacienteSeleccionado.getTipoIdentificacion() != null) {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
        } else {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
        }
        if (pacienteSeleccionado.getFechaNacimiento() != null) {
            datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
            datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
        } else {
            datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
            datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
        }
        strAux = "No refiere";
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
        }
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
        }
        datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
        datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
        if (pacienteSeleccionado.getIdAdministradora() != null) {
            datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
        } else {
            datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getEstadoCivil() != null) {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
        } else {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getOcupacion() != null) {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
        } else {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
        }
        datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
        datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
        if (pacienteSeleccionado.getGenero() != null) {
            datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
        } else {
            datosPrincipales.setGenero("<b>GENERO: </b> ");
        }
        //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
        datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
        datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
        datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
        datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
        listadoDatosAdicionales.add(buscarDetalle("MOTIVO DE CONSULTA", "motivo_consulta", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("ENFERMEDAD ACTUAL", "enf_actual", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("HISTORIA Y ANTECEDENTES FAMILIARES", "his_ant_fliares", listaDetalles));
        //DESARROLLO POSTNATAL(COMPUESTA)            
        listadoDatosAdicionales.add(buscarDetalle("FUNCIONES MOTORAS", "fun_motoras", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("FUNCIONES DEL LENGUAJE", "fun_lenguaje", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("CONDUCTA SOCIAL Y EMOCIONAL", "cond_soc_emoc", listaDetalles));
        //ANTECEDENTES(COMPUESTA)
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("MEDICOS", "ant_medicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("QUIRURGICOS", "ant_quirurjico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TRANSFUNCIONALES", "ant_transfuncionales", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("INMUNOLOGICOS", "ant_inmunologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("ALERGICOS", "ant_alergicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TRAUMATICOS", "ant_traumaticos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("PSICOLOGICOS", "ant_psicologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("FARMACOLOGICOS", "ant_farmacologico", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("FAMILIARES", "ant_familiares", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("TOXICOS", "ant_toxicos", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("OTROS", "ant_otros", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("ANTECEDENTES", strAux));
        listadoDatosAdicionales.add(buscarDetalle("REVISION POR SISTEMA", "revision_sistema", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("ANALISIS (CONDUCTA DEL SUEÑO, ALIMENTACION, CONTROL ESFINTE)", "analisis", listaDetalles));
        //IMPRESION DIAGNOSTICA(COMPUESTA)            
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO PPAL", "imp_diag_ppal", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 1", "imp_diag_1", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 2", "imp_diag_2", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 3", "imp_diag_3", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("IMPRESION DIAGNOSTICA", strAux));
        listadoDatosAdicionales.add(buscarDetalle("PLAN DE TRATAMIENTO", "plan_trat", listaDetalles));
        datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosReferencia(HcRegistro regEncontrado) {
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
        datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
        datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
        if (regEncontrado.getIdMedico() != null) {
            datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
            if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
            }
            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

            if (regEncontrado.getIdMedico().getFirma() != null) {
                datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
            }
        }
        datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
        datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
        if (pacienteSeleccionado.getTipoIdentificacion() != null) {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
        } else {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
        }
        if (pacienteSeleccionado.getFechaNacimiento() != null) {
            datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
            datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
        } else {
            datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
            datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
        }
        strAux = "No refiere";
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
        }
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
        }
        datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
        datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
        if (pacienteSeleccionado.getIdAdministradora() != null) {
            datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
        } else {
            datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getEstadoCivil() != null) {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
        } else {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getOcupacion() != null) {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
        } else {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
        }
        datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
        datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
        if (pacienteSeleccionado.getGenero() != null) {
            datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
        } else {
            datosPrincipales.setGenero("<b>GENERO: </b> ");
        }
        //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
        //datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
        //datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
        //datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
        //datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
        //datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
        datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
        datosPrincipales.setFechaNacimiento(datosPrincipales.getGenero());
        datosPrincipales.setLugarNacimiento(datosPrincipales.getTel());
        datosPrincipales.setEps(getHtmlDesdeDatoAdicional(buscarDetalle("INSTITUCION DE ORIGEN", "ins_origen", listaDetalles)));
        datosPrincipales.setGenero(getHtmlDesdeDatoAdicional(buscarDetalle("PROFESIONAL SOLICITANTE", "prof_solic", listaDetalles)));
        datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("ESPECIALIDAD SOLICITADA", "esp_sol", listaDetalles)));
        datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("REFERENCIA O CONTRAREFERENCIA", "tip_ref", listaDetalles)));
        datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("DX CIE 10", "diag_ppal", listaDetalles)));
        datosPrincipales.setEstadoCivil("");
        datosPrincipales.setTel("");
        datosPrincipales.setOcupacion("");
        datosPrincipales.setDireccion("");
        datosPrincipales.setNumeroAutorizacion("");
        //listadoDatosAdicionales.add(buscarDetalle("DIAGNOSTICO", "diag_text", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("RESUMEN COMPLETO HISTORIA CLINICA", "res_hc", listaDetalles));
        datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosNotaPsiquiatria(HcRegistro regEncontrado) {
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
        datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
        datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
        if (regEncontrado.getIdMedico() != null) {
            datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
            if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
            }
            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

            if (regEncontrado.getIdMedico().getFirma() != null) {
                datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
            }
        }
        datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
        datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
        if (pacienteSeleccionado.getTipoIdentificacion() != null) {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
        } else {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
        }
        if (pacienteSeleccionado.getFechaNacimiento() != null) {
            datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
            datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
        } else {
            datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
            datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
        }
        strAux = "No refiere";
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
        }
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
        }
        datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
        datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
        if (pacienteSeleccionado.getIdAdministradora() != null) {
            datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
        } else {
            datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getEstadoCivil() != null) {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
        } else {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getOcupacion() != null) {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
        } else {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
        }
        datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
        datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
        if (pacienteSeleccionado.getGenero() != null) {
            datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
        } else {
            datosPrincipales.setGenero("<b>GENERO: </b> ");
        }
        //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
        //datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
        //datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
        //datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
        datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "servicio", listaDetalles)));
        datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "autorizacion", listaDetalles)));
        datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
        datosPrincipales.setColegio(datosPrincipales.getDireccion());
        datosPrincipales.setEscolaridad(datosPrincipales.getTel());
        if (pacienteSeleccionado.getAcompanante() != null) {
            datosPrincipales.setEstadoCivil("<b>ACOMPAÑANTE O ACUDIENTE</b>: " + pacienteSeleccionado.getAcompanante());//ACOMPAÑANTE O ACUDIENTE
        } else {
            datosPrincipales.setEstadoCivil("<b>ACOMPAÑANTE O ACUDIENTE</b>: ");
        }
        if (pacienteSeleccionado.getParentesco() != null) {
            datosPrincipales.setOcupacion("<b>PARENTEZCO</b>: " + pacienteSeleccionado.getParentesco().getDescripcion());//PARENTEZCO
        } else {
            datosPrincipales.setOcupacion("<b>PARENTEZCO</b>: ");//PARENTEZCO
        }
        datosPrincipales.setDireccion(
                "<b>NUMERO DE SESION: </b>"
                + buscarDetalle("NUM SESION", "num_sesion", listaDetalles).getContenido()
                + " <b>DE: </b> "
                + buscarDetalle("NUM SESION", "de", listaDetalles).getContenido());//SESION TAL DE TAL
        datosPrincipales.setTel("");
        datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "servicio", listaDetalles)));
        listadoDatosAdicionales.add(buscarDetalle("DETALLE CONSULTA", "nota", listaDetalles));
        listadoDatosAdicionales.add(buscarDetalle("ANALISIS", "analisis", listaDetalles));
        //IMPRESION DIAGNOSTICA(COMPUESTA)            
        strAux = getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO PPAL", "imp_diag_ppal", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 1", "imp_diag_1", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 2", "imp_diag_2", listaDetalles)) + "<br/>";
        strAux = strAux + getHtmlDesdeDatoAdicional(buscarDetalle("DIAGNOSTICO RELACIONADO 3", "imp_diag_3", listaDetalles));
        listadoDatosAdicionales.add(new DatosAdicionalesReporteHistoria("IMPRESION DIAGNOSTICA", strAux));
        listadoDatosAdicionales.add(buscarDetalle("PLAN DE TRATAMIENTO", "plan_trat", listaDetalles));
        datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosNotaControl(HcRegistro regEncontrado) {
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
        datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
        datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
        if (regEncontrado.getIdMedico() != null) {
            datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
            if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
            }
            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

            if (regEncontrado.getIdMedico().getFirma() != null) {
                datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
            }
        }
        datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
        datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
        if (pacienteSeleccionado.getTipoIdentificacion() != null) {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
        } else {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
        }
        if (pacienteSeleccionado.getFechaNacimiento() != null) {
            datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
            datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
        } else {
            datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
            datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
        }
        strAux = "No refiere";
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
        }
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
        }
        datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
        datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
        if (pacienteSeleccionado.getIdAdministradora() != null) {
            datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
        } else {
            datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getEstadoCivil() != null) {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
        } else {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getOcupacion() != null) {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
        } else {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
        }
        datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
        datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
        if (pacienteSeleccionado.getGenero() != null) {
            datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
        } else {
            datosPrincipales.setGenero("<b>GENERO: </b> ");
        }
        //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
        //datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
        //datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
        //datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
        datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "servicio", listaDetalles)));
        datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "autorizacion", listaDetalles)));
        datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
        datosPrincipales.setColegio(datosPrincipales.getDireccion());
        datosPrincipales.setEscolaridad(datosPrincipales.getTel());
        if (pacienteSeleccionado.getAcompanante() != null) {
            datosPrincipales.setEstadoCivil("<b>ACOMPAÑANTE O ACUDIENTE</b>: " + pacienteSeleccionado.getAcompanante());//ACOMPAÑANTE O ACUDIENTE
        } else {
            datosPrincipales.setEstadoCivil("<b>ACOMPAÑANTE O ACUDIENTE</b>: ");
        }
        if (pacienteSeleccionado.getParentesco() != null) {
            datosPrincipales.setOcupacion("<b>PARENTEZCO</b>: " + pacienteSeleccionado.getParentesco().getDescripcion());//PARENTEZCO
        } else {
            datosPrincipales.setOcupacion("<b>PARENTEZCO</b>: ");//PARENTEZCO
        }
        datosPrincipales.setDireccion(
                "<b>NUMERO DE SESION: </b>"
                + buscarDetalle("NUM SESION", "num_sesion", listaDetalles).getContenido()
                + " <b>DE: </b> "
                + buscarDetalle("NUM SESION", "de", listaDetalles).getContenido());//SESION TAL DE TAL
        datosPrincipales.setTel("");
        datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "servicio", listaDetalles)));
        listadoDatosAdicionales.add(buscarDetalle("EVOLUCION / NOTA", "nota", listaDetalles));
        datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private void cargarFuenteDatosRecetario(HcRegistro regEncontrado) {
        listaRegistrosParaPdf = new ArrayList<>();
        DatosPrincipalesReporteHistoria datosPrincipales = new DatosPrincipalesReporteHistoria();
        String strAux;
        List<HcDetalle> listaDetalles = regEncontrado.getHcDetalleList();
        List<DatosAdicionalesReporteHistoria> listadoDatosAdicionales = new ArrayList<>();
        //CARGAR DATOS PRINCIPALES DIRECTAMENTE (a partir de paciente o registro)-----------------------------------------------------------------------
        datosPrincipales.setLogoEmpresa(loginMB.getRutaCarpetaImagenes() + loginMB.getEmpresaActual().getLogo().getUrlImagen());
        datosPrincipales.setTituloRegistro(regEncontrado.getIdTipoReg().getNombre());
        if (regEncontrado.getIdMedico() != null) {
            datosPrincipales.setMedico(regEncontrado.getIdMedico().nombreCompleto());
            if (regEncontrado.getIdMedico().getEspecialidad() != null) {
                datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/>" + regEncontrado.getIdMedico().getEspecialidad().getDescripcion());
            }
            datosPrincipales.setMedico(datosPrincipales.getMedico() + "<br/> Reg. prof. " + obtenerCadenaNoNula(regEncontrado.getIdMedico().getRegistroProfesional()));

            if (regEncontrado.getIdMedico().getFirma() != null) {
                datosPrincipales.setFirmaMedico(loginMB.getRutaCarpetaImagenes() + regEncontrado.getIdMedico().getFirma().getUrlImagen());
            }
        }
        datosPrincipales.setNombres("<b>NOMBRES: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerNombre(), pacienteSeleccionado.getSegundoNombre()));
        datosPrincipales.setApellidos("<b>APELLIDOS: </b>" + obtenerCadenaNoNula(pacienteSeleccionado.getPrimerApellido(), pacienteSeleccionado.getSegundoApellido()));
        if (pacienteSeleccionado.getTipoIdentificacion() != null) {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getTipoIdentificacion().getDescripcion() + " " + pacienteSeleccionado.getIdentificacion());
        } else {
            datosPrincipales.setIdentificacion("<b>IDENTIFICACION: </b> " + pacienteSeleccionado.getIdentificacion());
        }
        if (pacienteSeleccionado.getFechaNacimiento() != null) {
            datosPrincipales.setEdad("<b>EDAD: </b> " + calcularEdad(pacienteSeleccionado.getFechaNacimiento()));
            datosPrincipales.setFechaNacimiento("<b>FECHA NACIMIENTO: </b> " + sdfDate.format(pacienteSeleccionado.getFechaNacimiento()));
        } else {
            datosPrincipales.setEdad("<b>FECHA NACIMIENTO: </b> No refiere");
            datosPrincipales.setFechaNacimiento("<b>EDAD: </b> No refiere");
        }
        strAux = "No refiere";
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = pacienteSeleccionado.getMunNacimiento().getDescripcion();
        }
        if (pacienteSeleccionado.getMunNacimiento() != null) {
            strAux = strAux + "(" + pacienteSeleccionado.getMunNacimiento().getDescripcion() + ")";
        }
        datosPrincipales.setLugarNacimiento("<b>LUGAR NACIMIENTO: </b> " + strAux);
        datosPrincipales.setFechaRegistro("<b>FECHA REG CLINICO: </b> " + sdfDateHour.format(regEncontrado.getFechaReg()));
        if (pacienteSeleccionado.getIdAdministradora() != null) {
            datosPrincipales.setEps("<b>EPS: </b> " + pacienteSeleccionado.getIdAdministradora().getRazonSocial());
        } else {
            datosPrincipales.setEps("<b>EPS: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getEstadoCivil() != null) {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + pacienteSeleccionado.getEstadoCivil().getDescripcion());
        } else {
            datosPrincipales.setEstadoCivil("<b>ESTADO CIVIL: </b> " + "No refiere");
        }
        if (pacienteSeleccionado.getOcupacion() != null) {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + pacienteSeleccionado.getOcupacion().getDescripcion());
        } else {
            datosPrincipales.setOcupacion("<b>OCUPACION: </b> " + "No refiere");
        }
        datosPrincipales.setDireccion("<b>DIRECCION: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getDireccion()));
        datosPrincipales.setTel("<b>TELEFONO: </b> " + obtenerCadenaNoNula(pacienteSeleccionado.getTelefonoResidencia()));
        if (pacienteSeleccionado.getGenero() != null) {
            datosPrincipales.setGenero("<b>GENERO: </b> " + pacienteSeleccionado.getGenero().getDescripcion());
        } else {
            datosPrincipales.setGenero("<b>GENERO: </b> ");
        }
        //CARGAR DATOS PRINCIPALES POR BUSQUEDA EN hc_detalle-----------------------------------------------------------------------
        //datosPrincipales.setLateridad(getHtmlDesdeDatoAdicional(buscarDetalle("LATERALIDAD", "lateridad", listaDetalles)));
        //datosPrincipales.setColegio(getHtmlDesdeDatoAdicional(buscarDetalle("COLEGIO", "colegio", listaDetalles)));
        //datosPrincipales.setEscolaridad(getHtmlDesdeDatoAdicional(buscarDetalle("ESCOLARIDAD", "escolaridad", listaDetalles)));
        //datosPrincipales.setServicio(getHtmlDesdeDatoAdicional(buscarDetalle("SERVICIO", "tipo_consulta", listaDetalles)));
        //datosPrincipales.setNumeroAutorizacion(getHtmlDesdeDatoAdicional(buscarDetalle("NUMERO AUTORIZACION", "num_autorizacion", listaDetalles)));
        datosPrincipales.setDirTelEmpresa("<b>Dirección: </b> " + empresa.getDireccion() + "<br/>" + "<b>Teléfono: </b> " + empresa.getTelefono1() + " - " + empresa.getTelefono2());
        listadoDatosAdicionales.add(buscarDetalle("DETALLES RECETA", "recetario_txt", listaDetalles));
        datosPrincipales.setListaDatosAdicionales(listadoDatosAdicionales);
        listaRegistrosParaPdf.add(datosPrincipales);
    }

    private String getHtmlDesdeDatoAdicional(DatosAdicionalesReporteHistoria adicional) {
        return "<b>" + adicional.getTitulo() + ": </b>" + adicional.getContenido();
    }

    private DatosAdicionalesReporteHistoria buscarDetalle(String nomTitulo, String nomCampo, List<HcDetalle> listaDetalles) {
        //se busca si existe el valor para un campo del pdf en la lista de detalles para el registro actual        
        for (HcDetalle detalle : listaDetalles) {//Se lo busca en la lista de detalles
            if (detalle.getHcCamposReg().getTabla() != null && detalle.getHcCamposReg().getNombre().compareTo(nomCampo) == 0) {//si hc_campos_reg.tabla != null es por que es una tabla categorica y se debe realizar busqueda
                switch (detalle.getHcCamposReg().getTabla()) {
                    case "cfg_clasificaciones":
                        clasificacionBuscada = clasificacionesFacade.find(Integer.parseInt(detalle.getValor()));
                        if (clasificacionBuscada != null) {
                            return new DatosAdicionalesReporteHistoria(nomTitulo, clasificacionBuscada.getDescripcion());
                        } else {
                            return new DatosAdicionalesReporteHistoria(nomTitulo, "No remite");
                        }
                    case "fac_servicio":
                        servicioBuscado = servicioFacade.find(Integer.parseInt(detalle.getValor()));
                        if (servicioBuscado != null) {
                            return new DatosAdicionalesReporteHistoria(nomTitulo, servicioBuscado.getNombreServicio());
                        } else {
                            return new DatosAdicionalesReporteHistoria(nomTitulo, "No remite");
                        }
                }
            }
            if (detalle.getHcCamposReg().getNombre().compareTo(nomCampo) == 0) {//solo se saca el valor del detalle
                return new DatosAdicionalesReporteHistoria(nomTitulo, detalle.getValor());
            }
        }
        return new DatosAdicionalesReporteHistoria(nomTitulo, "No refiere");//no se encuentra este dato, esta vacio => No refiere
    }

    public void generarPdf() throws JRException, IOException {//genera un pdf de una historia seleccionada en el historial        

        if (cargarFuenteDeDatos()) {//si se puede cargar datos continuo
            JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listaRegistrosParaPdf);
            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            try (ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream()) {
                httpServletResponse.setContentType("application/pdf");
                ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
                JasperPrint jasperPrint = JasperFillManager.fillReport(servletContext.getRealPath("historias/reportes/reporteRegCli.jasper"), new HashMap(), beanCollectionDataSource);
                JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
                FacesContext.getCurrentInstance().responseComplete();
            }
        }
    }

    public void generarPdfAgrupado() throws JRException, IOException {//genera un pdf de un serie de historias seleccionadas en el historial        
        if (cargarFuenteDeDatosAgrupada()) {//si se puede cargar datos continuo
            JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listaRegistrosParaPdf);
            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            try (ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream()) {
                httpServletResponse.setContentType("application/pdf");
                ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
                JasperPrint jasperPrint = JasperFillManager.fillReport(servletContext.getRealPath("historias/reportes/reporteRegCliAgrupado.jasper"), new HashMap(), beanCollectionDataSource);
                JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
                FacesContext.getCurrentInstance().responseComplete();
            }
        }
    }

    //---------------------------------------------------
    //--------- FUNCIONES REGISTROS CLINICOS ------------
    //---------------------------------------------------
    public void limpiarFormulario() {//reinicia todos los controles de un registro clinico

        modificandoRegCli = false;
        registroEncontrado = null;
        estructuraCampos.limpiar();
        idPrestador = null;
        especialidadPrestador = null;
        fechaReg = new Date();
        fechaSis = new Date();
        //seleccionar medico de ser posible
        if (loginMB.getUsuarioActual().getTipoUsuario().getCodigo().equals("2")) {//es un prestador
            for (CfgUsuarios prestador : listaPrestadores) {
                if (Objects.equals(prestador.getIdUsuario(), loginMB.getUsuarioActual().getIdUsuario())) {
                    idPrestador = loginMB.getUsuarioActual().getIdUsuario().toString();
                    if (prestador.getEspecialidad() != null) {
                        especialidadPrestador = prestador.getEspecialidad().getDescripcion();
                    }
                    break;
                }
            }
        }
    }

    public void cambiaTipoRegistroClinico() {//cambia el combo 'tipo de registro clinico'
        limpiarFormulario();
        cargarUltimoRegistro();
        modificandoRegCli = false;
    }

    private void cargarUltimoRegistro() {
        mostrarFormularioRegistroClinico();
        if (tipoRegistroClinicoActual != null) {//validacion particular para determinar si es "HISTORIA CLINICA PSIQUIATRIA" y asignar por defecto 
            if (tipoRegistroClinicoActual.getIdTipoReg() == 5) {
                estructuraCampos.setCampo0("17");//en este combo quede seleccionado PSIQUIATRIA CONTROL
            }
        }
        if (tipoRegistroClinicoActual != null) {
            registroEncontrado = registroFacade.buscarUltimo(pacienteSeleccionado.getIdPaciente(), tipoRegistroClinicoActual.getIdTipoReg());
            if (registroEncontrado != null) {
                //cargar los datos adicionales a estructuraCampos
                List<HcDetalle> listaDetalles = registroEncontrado.getHcDetalleList();
                for (HcDetalle detalle : listaDetalles) {
                    HcCamposReg campo = camposRegFacade.find(detalle.getHcDetallePK().getIdCampo());
                    estructuraCampos.setValor(campo.getPosicion(), detalle.getValor());
                }
                imprimirMensaje("Informacion", "Para su facilidad se cargo los datos de la última historia de este tipo de registro", FacesMessage.SEVERITY_INFO);
            } else {
                imprimirMensaje("Informacion", "El paciente no tiene registros anteriores de este tipo", FacesMessage.SEVERITY_INFO);
            }
        }
        //RequestContext.getCurrentInstance().execute("PF('dlgHistorico').hide();");
        RequestContext.getCurrentInstance().update("IdFormHistorias");

    }

    private void mostrarFormularioRegistroClinico() {//permite visualizar un formulario dependiendo de la seleccion en el combo 'tipo de registro clinico'
        if (tipoRegistroClinico != null && tipoRegistroClinico.length() != 0) {
            tipoRegistroClinicoActual = tipoRegCliFacade.find(Integer.parseInt(tipoRegistroClinico));
            urlPagina = tipoRegistroClinicoActual.getUrlPagina();
            nombreTipoRegistroClinico = tipoRegistroClinicoActual.getNombre();
        } else {
            tipoRegistroClinicoActual = null;
            urlPagina = "";
            nombreTipoRegistroClinico = "";
        }
    }

    public void cambiaMedico() {//cambia el combo 'medico'(actualiza el campo especialidad)
        if (idPrestador != null && idPrestador.length() != 0) {
            especialidadPrestador = usuariosFacade.find(Integer.parseInt(idPrestador)).getEspecialidad().getDescripcion();
        } else {
            especialidadPrestador = "";
        }
    }

    public void actualizarRegistro() {//actualizacion de un registro clinico existente
        List<HcDetalle> listaDetalle = new ArrayList<>();
        HcDetalle nuevoDetalle;
        HcCamposReg campoResgistro;

        if (!modificandoRegCli) {
            imprimirMensaje("Error", "No se ha cargado un registro para poder modificarlo", FacesMessage.SEVERITY_ERROR);
            return;
        }
        //nuevoRegistro=
        if (idPrestador != null && idPrestador.length() != 0) {//validacion de campos obligatorios
            registroEncontrado.setIdMedico(usuariosFacade.find(Integer.parseInt(idPrestador)));
        } else {
            imprimirMensaje("Error", "Debe seleccionar un médico", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (tipoRegistroClinicoActual != null) {
            registroEncontrado.setIdTipoReg(tipoRegistroClinicoActual);
        } else {
            imprimirMensaje("Error", "No se puede determinar el tipo de registro clinico", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (fechaReg == null) {
            fechaReg = fechaSis;
        }
        registroEncontrado.setFechaReg(fechaReg);
        registroEncontrado.setFechaSis(fechaSis);
        registroEncontrado.setIdPaciente(pacienteSeleccionado);
        registroFacade.edit(registroEncontrado);
        for (int i = 0; i < 50; i++) {
            if (estructuraCampos.getValor(i) != null && estructuraCampos.getValor(i).length() != 0) {
                campoResgistro = camposRegFacade.buscarPorTipoRegistroYPosicion(tipoRegistroClinicoActual.getIdTipoReg(), i);
                if (campoResgistro != null) {
                    nuevoDetalle = new HcDetalle(registroEncontrado.getIdRegistro(), campoResgistro.getIdCampo());
                    if (estructuraCampos.getValor(i).contains("<")) {
                        nuevoDetalle.setValor(corregirHtml(estructuraCampos.getValor(i)));
                    } else {
                        nuevoDetalle.setValor(estructuraCampos.getValor(i));
                    }
                    listaDetalle.add(nuevoDetalle);
                } else {
                    System.out.println("No encontro en tabla hc_campos_registro el valor: id_tipo_reg = " + tipoRegistroClinicoActual.getIdTipoReg());
                }
            }
        }
        registroEncontrado.setHcDetalleList(listaDetalle);
        registroFacade.edit(registroEncontrado);
        imprimirMensaje("Correcto", "Registro actualizado.", FacesMessage.SEVERITY_INFO);
        limpiarFormulario();
        RequestContext.getCurrentInstance().update("IdFormRegistroClinico");
    }

    public void guardarRegistro() {//guardar un nuevo registro clinico
        HcRegistro nuevoRegistro = new HcRegistro();
        List<HcDetalle> listaDetalle = new ArrayList<>();
        HcDetalle nuevoDetalle;
        HcCamposReg campoResgistro;
        if (idPrestador != null && idPrestador.length() != 0) {//validacion de campos obligatorios
            nuevoRegistro.setIdMedico(usuariosFacade.find(Integer.parseInt(idPrestador)));
        } else {
            imprimirMensaje("Error", "Debe seleccionar un médico", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (tipoRegistroClinicoActual != null) {
            nuevoRegistro.setIdTipoReg(tipoRegistroClinicoActual);
        } else {
            imprimirMensaje("Error", "No se puede determinar el tipo de registro clinico", FacesMessage.SEVERITY_ERROR);
            return;
        }
        if (fechaReg == null) {
            fechaReg = fechaSis;
        }
        nuevoRegistro.setFechaReg(fechaReg);
        nuevoRegistro.setFechaSis(fechaSis);
        nuevoRegistro.setIdPaciente(pacienteSeleccionado);

        if (validarNoVacio(turnoCita)) {
            List<CitCitas> listaCitas = turnosFacade.find(Integer.parseInt(turnoCita)).getCitCitasList();
            CitCitas citaAtendida = null;
            for (CitCitas cita : listaCitas) {
                if (cita.getCancelada() == false) {
                    nuevoRegistro.setIdCita(cita);
                    citaAtendida = cita;
                    break;
                }
            }
            if (citaAtendida != null) {
                citaAtendida.setAtendida(true);
                citaAtendida.setTieneRegAsociado(true);
//                citasFacade.edit(citaAtendida);
                agendaPrestadorMB.actualizarAutorizaciones(citaAtendida);
            }
        }
        registroFacade.create(nuevoRegistro);

        for (int i = 0; i < 50; i++) {
            if (estructuraCampos.getValor(i) != null && estructuraCampos.getValor(i).length() != 0) {
                campoResgistro = camposRegFacade.buscarPorTipoRegistroYPosicion(tipoRegistroClinicoActual.getIdTipoReg(), i);
                if (campoResgistro != null) {
                    nuevoDetalle = new HcDetalle(nuevoRegistro.getIdRegistro(), campoResgistro.getIdCampo());
                    if (estructuraCampos.getValor(i).contains("<")) {
                        nuevoDetalle.setValor(corregirHtml(estructuraCampos.getValor(i)));
                    } else {
                        nuevoDetalle.setValor(estructuraCampos.getValor(i));
                    }
                    listaDetalle.add(nuevoDetalle);
                } else {
                    System.out.println("No encontro en tabla hc_campos_registro el valor: id_tipo_reg=");
                }
            }
        }
        nuevoRegistro.setHcDetalleList(listaDetalle);
        registroFacade.edit(nuevoRegistro);
        imprimirMensaje("Correcto", "Nuevo registro almacenado.", FacesMessage.SEVERITY_INFO);
        limpiarFormulario();
        RequestContext.getCurrentInstance().update("IdFormRegistroClinico");
    }

    public List<String> autocompletarDiagnostico(String txt) {//retorna una lista con los diagnosticos que contengan el parametro txt
        if (txt != null && txt.length() > 2) {
            return diagnosticoFacade.autocompletarDiagnostico(txt);
        } else {
            return null;
        }
    }

    //---------------------------------------------------
    //------ FUNCIONES CARGAR/BUSCAR PACIENTES ----------
    //---------------------------------------------------
    public void validarIdentificacion() {//verifica si existe la identificacion de lo contrario abre un dialogo para seleccionar el paciente de una tabla
        pacienteTmp = pacientesFacade.buscarPorIdentificacion(identificacionPaciente);

        if (pacienteTmp != null) {
            tipoRegistroClinico = "";
            urlPagina = "";
            nombreTipoRegistroClinico = "";
            pacienteSeleccionadoTabla = pacienteTmp;
            hayPacienteSeleccionado = true;//se pueden mostrar opciones
            cargarPaciente();

        } else {
            RequestContext.getCurrentInstance().execute("PF('dlgSeleccionarPaciente').show();");
        }
    }

    public void editarPaciente() {//se invoca funcion javaScript(cargarPaciente -> home.xhtml) que carga la pestaña de pacientes y los datos del paciende seleccionado
        if (pacienteSeleccionado != null) {
            RequestContext.getCurrentInstance().execute("window.parent.cargarPaciente('Pacientes','configuraciones/pacientes.xhtml','" + pacienteSeleccionado.getIdPaciente() + "')");
        } else {
            hayPacienteSeleccionado = false;
            imprimirMensaje("Error", "Se debe seleccionar un paciente de la tabla", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void cargarPaciente() {//cargar un paciente desde del dialogo de buscar paciente o al digitar una identificacion valida(esta en pacientes)        
        if (pacienteSeleccionadoTabla != null) {
            turnoCita = "";
            pacienteSeleccionado = pacientesFacade.find(pacienteSeleccionadoTabla.getIdPaciente());
            urlPagina = "";
            identificacionPaciente = "";
            nombrePaciente = "Paciente";
            nombreTipoRegistroClinico = "";
            hayPacienteSeleccionado = true;
            identificacionPaciente = pacienteSeleccionado.getIdentificacion();
            if (pacienteSeleccionado.getTipoIdentificacion() != null) {
                tipoIdentificacion = pacienteSeleccionado.getTipoIdentificacion().getDescripcion();
            } else {
                tipoIdentificacion = "";
            }
            nombrePaciente = pacienteSeleccionado.nombreCompleto();
            if (pacienteSeleccionado.getGenero() != null) {
                generoPaciente = pacienteSeleccionado.getGenero().getObservacion();
            } else {
                generoPaciente = "";
            }
            if (pacienteSeleccionado.getFechaNacimiento() != null) {
                edadPaciente = calcularEdad(pacienteSeleccionado.getFechaNacimiento());
            } else {
                edadPaciente = "";
            }
            if (pacienteSeleccionado.getIdAdministradora() != null) {
                administradoraPaciente = pacienteSeleccionado.getIdAdministradora().getRazonSocial();
            } else {
                administradoraPaciente = "";
            }
//            tipoRegistroClinico = "";
            tipoRegistroClinico = "3";
            cambiaTipoRegistroClinico();
            if (!cargandoDesdeTab) {
                RequestContext.getCurrentInstance().execute("PF('dlgSeleccionarPaciente').hide();");
            }
        } else {
            hayPacienteSeleccionado = false;
            imprimirMensaje("Error", "Se debe seleccionar un paciente de la tabla", FacesMessage.SEVERITY_ERROR);
        }
    }

    //---------------------------------------------------
    //-------- FUNCIONES TEXTOS PREDEFINIDOS ------------
    //---------------------------------------------------
    public void seleccionEditor(String ed, String posVec) {//determinar cual 'p:editor' y la posicion en 'estructuraCampos' donde se usara el texto predefinido
        idEditorSeleccionado = ed;
        posListaTxtPredef = Integer.parseInt(posVec);//determinar la posicion en el vector 'estructuraCampos' al usar un texto predefinido

    }

    public void usarTextoPredefinido() {//asignar el texto predefinido a un 'p:editor'
        if (detalleTextoPredef != null && detalleTextoPredef.length() != 0) {
            estructuraCampos.setValor(posListaTxtPredef, detalleTextoPredef);
            RequestContext.getCurrentInstance().update("IdFormRegistroClinico:" + idEditorSeleccionado);//se actualiza el editor
            RequestContext.getCurrentInstance().execute("PF('dlgTextosPredefinidos').hide()");//se cierra el dialogo            
        } else {
            imprimirMensaje("Error", "El texto predefinido está vacio", FacesMessage.SEVERITY_ERROR);
        }
    }

    private void recargarMaestrosTxtPredefinidos() {//carga la lista de textos predefinidos (se usa en cualquier p:editor)
        listaMaestrosTxtPredefinidos = maestrosTxtPredefinidosFacade.findAll();
        idMaestroTextoPredef = "";
        nombreTextoPredefinido = "";
        cambiaMaestro();
    }

    public void cambiaMaestro() {//cambia la clasificacion cuando se esta en el dialogo de textos predefinidos
        idTextoPredef = "";
        if (idMaestroTextoPredef != null && idMaestroTextoPredef.length() != 0) {
            listaTxtPredefinidos = maestrosTxtPredefinidosFacade.find(Integer.parseInt(idMaestroTextoPredef)).getCfgTxtPredefinidosList();
            if (listaTxtPredefinidos != null && !listaTxtPredefinidos.isEmpty()) {
                idTextoPredef = listaTxtPredefinidos.get(0).getIdTxtPredefinido().toString();
            }
        } else {
            listaTxtPredefinidos = null;
        }
        cambiaTextoPredefinido();
    }

    public void cambiaTextoPredefinido() {//usado en dialogo 'textosPredefinidos' cuando cambia el combo de 'textos predefinidos', permite cargar el texto predefinido en el p:ditor 
        if (idTextoPredef != null && idTextoPredef.length() != 0) {
            txtPredefinidoActual = txtPredefinidosFacade.find(Integer.parseInt(idTextoPredef));
            detalleTextoPredef = txtPredefinidoActual.getDetalle();
            nombreTextoPredefinido = txtPredefinidoActual.getNombre();
        } else {
            txtPredefinidoActual = null;
            detalleTextoPredef = "";
            nombreTextoPredefinido = "";
        }
    }

    public void guardarTextoPredefinido() {//almacenar en la base de datos el texto predefinido
        if (txtPredefinidoActual != null) {//se cargo uno
            if (txtPredefinidoActual.getNombre().compareTo(nombreTextoPredefinido) == 0) {//son los mismos se actualiza
                txtPredefinidoActual.setDetalle(detalleTextoPredef);
                txtPredefinidosFacade.edit(txtPredefinidoActual);
                imprimirMensaje("Correcto", "El texto predefinido se ha actualizado", FacesMessage.SEVERITY_INFO);
            } else {
                if (nombreTextoPredefinido.trim().length() == 0) {
                    imprimirMensaje("Error", "Debe escribir un nombre para el texto predefinido", FacesMessage.SEVERITY_ERROR);
                } else {//es nuevo se debe crear
                    if (txtPredefinidosFacade.buscarPorNombre(nombreTextoPredefinido) != null) {
                        imprimirMensaje("Error", "Ya existe un texto predefinido con este nombre", FacesMessage.SEVERITY_ERROR);
                    } else {
                        CfgTxtPredefinidos nuevoPredefinido = new CfgTxtPredefinidos();
                        nuevoPredefinido.setNombre(nombreTextoPredefinido);
                        nuevoPredefinido.setIdMaestro(txtPredefinidoActual.getIdMaestro());
                        nuevoPredefinido.setDetalle(detalleTextoPredef);
                        txtPredefinidosFacade.create(nuevoPredefinido);
                        recargarMaestrosTxtPredefinidos();
                        RequestContext.getCurrentInstance().update("IdFormRegistroClinico:IdPanelTextosPredefinidos");//se actualiza el editor
                        imprimirMensaje("Correcto", "El nuevo texto predefinido ha sido creado", FacesMessage.SEVERITY_INFO);
                    }
                }
            }
        } else {//se debe agregar a la categoria
            if (idMaestroTextoPredef != null && idMaestroTextoPredef.length() != 0) {
                if (nombreTextoPredefinido.trim().length() == 0) {
                    imprimirMensaje("Error", "Debe escribir un nombre para el texto predefinido", FacesMessage.SEVERITY_ERROR);
                } else {//es nuevo se debe crear
                    if (txtPredefinidosFacade.buscarPorNombre(nombreTextoPredefinido) != null) {
                        imprimirMensaje("Error", "Ya existe un texto predefinido con este nombre", FacesMessage.SEVERITY_ERROR);
                    } else {
                        CfgTxtPredefinidos nuevoPredefinido = new CfgTxtPredefinidos();
                        nuevoPredefinido.setNombre(nombreTextoPredefinido);
                        nuevoPredefinido.setIdMaestro(maestrosTxtPredefinidosFacade.find(Integer.parseInt(idMaestroTextoPredef)));
                        nuevoPredefinido.setDetalle(detalleTextoPredef);
                        txtPredefinidosFacade.create(nuevoPredefinido);
                        recargarMaestrosTxtPredefinidos();
                        RequestContext.getCurrentInstance().update("IdFormRegistroClinico:IdPanelTextosPredefinidos");//se actualiza el editor
                        imprimirMensaje("Correcto", "El nuevo texto predefinido ha sido creado", FacesMessage.SEVERITY_INFO);
                    }
                }
            } else {
                imprimirMensaje("Error", "No se ha seleccionado ninguna categoría", FacesMessage.SEVERITY_ERROR);
            }
        }
    }

    public void eliminarPredefinido() {//eliminar un texto predefinido de una categoria seleccionada
        if (idTextoPredef != null && idTextoPredef.length() != 0) {
            txtPredefinidosFacade.remove(txtPredefinidosFacade.find(Integer.parseInt(idTextoPredef)));
            recargarMaestrosTxtPredefinidos();
            RequestContext.getCurrentInstance().update("IdFormRegistroClinico:IdPanelTextosPredefinidos");//se actualiza el editor
            imprimirMensaje("Correcto", "El texto predefinido se ha eliminado", FacesMessage.SEVERITY_INFO);
        } else {
            imprimirMensaje("Error", "No se ha seleccionado ningún texto predefinido", FacesMessage.SEVERITY_ERROR);
        }
    }

    //---------------------------------------------------
    //-----------------FUNCIONES GET SET ----------------
    //---------------------------------------------------
    public LazyDataModel<CfgPacientes> getListaPacientes() {
        return listaPacientes;
    }

    public void setListaPacientes(LazyDataModel<CfgPacientes> listaPacientes) {
        this.listaPacientes = listaPacientes;
    }

//    public List<CfgPacientes> getListaPacientes() {
//        return listaPacientes;
//    }
//
//    public void setListaPacientes(List<CfgPacientes> listaPacientes) {
//        this.listaPacientes = listaPacientes;
//    }
    public CfgPacientes getPacienteSeleccionado() {
        return pacienteSeleccionado;
    }

    public void setPacienteSeleccionado(CfgPacientes pacienteSeleccionado) {
        this.pacienteSeleccionado = pacienteSeleccionado;
    }

    public String getIdentificacionPaciente() {
        return identificacionPaciente;
    }

    public void setIdentificacionPaciente(String identificacionPaciente) {
        this.identificacionPaciente = identificacionPaciente;
    }

    public String getUrlPagina() {
        return urlPagina;
    }

    public void setUrlPagina(String urlPagina) {
        this.urlPagina = urlPagina;
    }

    public String getTipoRegistroClinico() {
        return tipoRegistroClinico;
    }

    public void setTipoRegistroClinico(String tipoRegistroClinico) {
        this.tipoRegistroClinico = tipoRegistroClinico;
    }

    public boolean isHayPacienteSeleccionado() {
        return hayPacienteSeleccionado;
    }

    public void setHayPacienteSeleccionado(boolean hayPacienteSeleccionado) {
        this.hayPacienteSeleccionado = hayPacienteSeleccionado;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public String getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(String tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getGeneroPaciente() {
        return generoPaciente;
    }

    public void setGeneroPaciente(String generoPaciente) {
        this.generoPaciente = generoPaciente;
    }

    public String getEdadPaciente() {
        return edadPaciente;
    }

    public void setEdadPaciente(String edadPaciente) {
        this.edadPaciente = edadPaciente;
    }

    public String getAdministradoraPaciente() {
        return administradoraPaciente;
    }

    public void setAdministradoraPaciente(String administradoraPaciente) {
        this.administradoraPaciente = administradoraPaciente;
    }

    public String getNombreTipoRegistroClinico() {
        return nombreTipoRegistroClinico;
    }

    public void setNombreTipoRegistroClinico(String nombreTipoRegistroClinico) {
        this.nombreTipoRegistroClinico = nombreTipoRegistroClinico;
    }

    public List<CfgMaestrosTxtPredefinidos> getListaMaestrosTxtPredefinidos() {
        return listaMaestrosTxtPredefinidos;
    }

    public void setListaMaestrosTxtPredefinidos(List<CfgMaestrosTxtPredefinidos> listaMaestrosTxtPredefinidos) {
        this.listaMaestrosTxtPredefinidos = listaMaestrosTxtPredefinidos;
    }

    public List<CfgTxtPredefinidos> getListaTxtPredefinidos() {
        return listaTxtPredefinidos;
    }

    public void setListaTxtPredefinidos(List<CfgTxtPredefinidos> listaTxtPredefinidos) {
        this.listaTxtPredefinidos = listaTxtPredefinidos;
    }

    public String getDetalleTextoPredef() {
        return detalleTextoPredef;
    }

    public void setDetalleTextoPredef(String detalleTextoPredef) {
        this.detalleTextoPredef = detalleTextoPredef;
    }

    public String getIdTextoPredef() {
        return idTextoPredef;
    }

    public void setIdTextoPredef(String idTextoPredef) {
        this.idTextoPredef = idTextoPredef;
    }

    public String getIdMaestroTextoPredef() {
        return idMaestroTextoPredef;
    }

    public void setIdMaestroTextoPredef(String idMaestroTextoPredef) {
        this.idMaestroTextoPredef = idMaestroTextoPredef;
    }

    public String getNombreTextoPredefinido() {
        return nombreTextoPredefinido;
    }

    public void setNombreTextoPredefinido(String nombreTextoPredefinido) {
        this.nombreTextoPredefinido = nombreTextoPredefinido;
    }

    public EstructList getEstructuraCampos() {
        return estructuraCampos;
    }

    public void setEstructuraCampos(EstructList estructuraCampos) {
        this.estructuraCampos = estructuraCampos;
    }

    public String getIdEditorSeleccionado() {
        return idEditorSeleccionado;
    }

    public void setIdEditorSeleccionado(String idEditorSeleccionado) {
        this.idEditorSeleccionado = idEditorSeleccionado;
    }

    public String getIdPrestador() {
        return idPrestador;
    }

    public void setIdPrestador(String idPrestador) {
        this.idPrestador = idPrestador;
    }

    public String getEspecialidadPrestador() {
        return especialidadPrestador;
    }

    public void setEspecialidadPrestador(String especialidadPrestador) {
        this.especialidadPrestador = especialidadPrestador;
    }

    public Date getFechaReg() {
        return fechaReg;
    }

    public void setFechaReg(Date fechaReg) {
        this.fechaReg = fechaReg;
    }

    public Date getFechaSis() {
        return fechaSis;
    }

    public void setFechaSis(Date fechaSis) {
        this.fechaSis = fechaSis;
    }

    public String[] getRegCliSelHistorial() {
        return regCliSelHistorial;
    }

    public void setRegCliSelHistorial(String[] regCliSelHistorial) {
        this.regCliSelHistorial = regCliSelHistorial;
    }

    public TreeNode getTreeNodeRaiz() {
        return treeNodeRaiz;
    }

    public void setTreeNodeRaiz(TreeNode treeNodeRaiz) {
        this.treeNodeRaiz = treeNodeRaiz;
    }

    public TreeNode[] getTreeNodesSeleccionados() {
        return treeNodesSeleccionados;
    }

    public void setTreeNodesSeleccionados(TreeNode[] treeNodesSeleccionados) {
        this.treeNodesSeleccionados = treeNodesSeleccionados;
    }

    public boolean isModificandoRegCli() {
        return modificandoRegCli;
    }

    public void setModificandoRegCli(boolean modificandoRegCli) {
        this.modificandoRegCli = modificandoRegCli;
    }

    public LoginMB getLoginMB() {
        return loginMB;
    }

    public void setLoginMB(LoginMB loginMB) {
        this.loginMB = loginMB;
    }

    public boolean isBtnHistorialDisabled() {
        return btnHistorialDisabled;
    }

    public void setBtnHistorialDisabled(boolean btnHistorialDisabled) {
        this.btnHistorialDisabled = btnHistorialDisabled;
    }

    public CfgPacientes getPacienteSeleccionadoTabla() {
        return pacienteSeleccionadoTabla;
    }

    public void setPacienteSeleccionadoTabla(CfgPacientes pacienteSeleccionadoTabla) {
        this.pacienteSeleccionadoTabla = pacienteSeleccionadoTabla;
    }

    public boolean isBtnPdfAgrupadoDisabled() {
        return btnPdfAgrupadoDisabled;
    }

    public void setBtnPdfAgrupadoDisabled(boolean btnPdfAgrupadoDisabled) {
        this.btnPdfAgrupadoDisabled = btnPdfAgrupadoDisabled;
    }

    public String getFiltroAutorizacion() {
        return filtroAutorizacion;
    }

    public void setFiltroAutorizacion(String filtroAutorizacion) {
        this.filtroAutorizacion = filtroAutorizacion;
    }

    public Date getFiltroFechaInicial() {
        return filtroFechaInicial;
    }

    public void setFiltroFechaInicial(Date filtroFechaInicial) {
        this.filtroFechaInicial = filtroFechaInicial;
    }

    public Date getFiltroFechaFinal() {
        return filtroFechaFinal;
    }

    public void setFiltroFechaFinal(Date filtroFechaFinal) {
        this.filtroFechaFinal = filtroFechaFinal;
    }

    public boolean isBtnEditarRendered() {
        return btnEditarRendered;
    }

    public void setBtnEditarRendered(boolean btnEditarRendered) {
        this.btnEditarRendered = btnEditarRendered;
    }

    public List<CfgUsuarios> getListaPrestadores() {
        return listaPrestadores;
    }

    public void setListaPrestadores(List<CfgUsuarios> listaPrestadores) {
        this.listaPrestadores = listaPrestadores;
    }

    public String getTurnoCita() {
        return turnoCita;
    }

    public void setTurnoCita(String turnoCita) {
        this.turnoCita = turnoCita;
    }

    public AgendaPrestadorMB getAgendaPrestadorMB() {
        return agendaPrestadorMB;
    }

    public void setAgendaPrestadorMB(AgendaPrestadorMB agendaPrestadorMB) {
        this.agendaPrestadorMB = agendaPrestadorMB;
    }

}
