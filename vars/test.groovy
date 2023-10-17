pipeline {
    agent {
        node {
            label 'hwci_p01'
        }
    }
    environment{
        CREDS = credentials('soctest-creds-for-artifactory'); //system user ID
        ARTY_USERNAME = "$CREDS_USR";
        ARTY_PASSWORD = "$CREDS_PSW";
    }
    options {
        disableConcurrentBuilds()
        timeout(time: 4, unit: 'HOURS') 
    }

    stages {
        stage ('Envs Check"') {
            steps {
                catchError {
                sh label: '', script: '''#!/bin/bash
                . /projects/ddm/util/.bashrc.project
                WHOAMI=`whoami` ;
                echo ' * INFO :: WHOAMI            === '${WHOAMI}  ;
                echo ' * INFO :: JOB_NAME          === '${JOB_NAME}  ;
                echo ' * INFO :: NODE_NAME         === '${NODE_NAME}  ;
                echo ' * INFO :: WORKSPACE         === '${WORKSPACE}  ;
                echo ' * INFO :: clone_path         === '${CLONE_PATH}  ;
                echo ' * INFO :: repo_branch         === '$branch  ;
                export WORK_DIR=${WORK_DIR_PREFIX}/${WHOAMI}/${WORK_DIR_MIDFIX_0}/${NODE_NAME}/${WORK_DIR_MIDFIX_1}/${JOB_NAME} ;
                echo ' * INFO :: WORK_DIR          === '${WORK_DIR}  ;
                echo ' * INFO :: REGRESSION_TYPE   === '${REGRESSION_TYPE}  ;
                echo ' * INFO :: REFERENCE_IP_NAME === '${REFERENCE_IP_NAME}  ;
                echo ' * INFO :: BUILD_ID_NFS_SHARE_CACHE_PRESERVE === '${BUILD_ID_NFS_SHARE_CACHE_PRESERVE}  ;
                echo ' * INFO :: BUILD_ID_COVERAGE_ON === '${BUILD_ID_COVERAGE_ON}  ;
                echo ' * INFO :: ARTY_USERNAME        === '${ARTY_USERNAME}  ;
                echo ' * INFO :: ARTY_PASSWORD        === '${ARTY_PASSWORD}  ;
                #
                # -- forcible definition of $RegressionMainCategory { SANITY | FULL }
                #
                if [[ ${REGRESSION_TYPE} == *"SANITY"* ]]; then
                export RegressionMainCategory=SANITY ;
                else
                export RegressionMainCategory=FULL ;
                fi
                echo -n "Executing CASE ${RegressionMainCategory} resulting merge ops ..."
                '''
                }
            }
        }
        stage("Arty Creds Caching for Node Execution") {
            when {
                environment name: 'BUILD_ID_STORE_ARTIFACTS', value: 'true'
            }
            steps {
                catchError {
                sh label: '', script: '''#!/bin/bash
                . /projects/ddm/util/.bashrc.project
                export CRDIR='.jfrog'
                export CRFILE='jfrog-cli.conf'
                mkdir -p $HOME/${CRDIR}
                echo '{'                                                                                              > $HOME/${CRDIR}/${CRFILE}
                echo '  "artifactory": ['                                                                            >> $HOME/${CRDIR}/${CRFILE}
                echo '    {'                                                                                         >> $HOME/${CRDIR}/${CRFILE}
                echo '      "url": "https://artifactory-espoo1.int.net.nokia.com/artifactory/soc-reporting-local/",' >> $HOME/${CRDIR}/${CRFILE}
                echo '      "user": "'${ARTY_USERNAME}'",'                                                           >> $HOME/${CRDIR}/${CRFILE}
                echo '      "password": "'${ARTY_PASSWORD}'",'                                                       >> $HOME/${CRDIR}/${CRFILE}
                echo '      "serverId": "soc-reporting-local",'                                                      >> $HOME/${CRDIR}/${CRFILE}
                echo '      "isDefault": true'                                                                       >> $HOME/${CRDIR}/${CRFILE}
                echo '    }'                                                                                         >> $HOME/${CRDIR}/${CRFILE}
                echo '  ],'                                                                                          >> $HOME/${CRDIR}/${CRFILE}
                echo '  "Version": "1"'                                                                              >> $HOME/${CRDIR}/${CRFILE}
                echo '}'                                                                                             >> $HOME/${CRDIR}/${CRFILE}
                chmod 700 -R $HOME/${CRDIR}
                '''
                }
            }
        }
        stage("Checkout") {
            steps {
                catchError {
                sh label: '', script: '''#!/bin/bash
                . /projects/ddm/util/.bashrc.project
                #
                module purge
                module load grp
                #
                WHOAMI=`whoami` ;
                export WORK_DIR=${WORK_DIR_PREFIX}/${WHOAMI}/${WORK_DIR_MIDFIX_0}/${NODE_NAME}/${WORK_DIR_MIDFIX_1}/${JOB_NAME} ;
                #
                mkdir -p ${WORK_DIR}
                echo -n ' * INFO :: pwd #0 === ' ; pwd ;
                cd ${WORK_DIR} ;
                echo -n ' * INFO :: pwd #1 === ' ; pwd ;
                BUILD_DIR=${REFERENCE_IP_NAME}"_JENKINS_"${BUILD_ID} ;
                if [ -d "$BUILD_DIR" ] ; then
                  echo "$BUILD_DIR exists on your filesystem. Deleting it as an obsolete and conflicting NFS Share relic."
                  rm -r -f ${BUILD_DIR}
                fi
                mkdir ${BUILD_DIR} ;
                cd ${BUILD_DIR} ;
                echo -n ' * INFO :: pwd #2 === ' ; pwd ;
                echo '################### Fetching started ##################'
                #grp clone -p subsystem/jesd/top_conf $branch; grp ws; 
                ##clone repo
                grp clone -p $CLONE_PATH $branch; grp ws;
                export WS_ROOT=${WORK_DIR}/${BUILD_DIR} ;
                export MODULES_PATH=${WORK_DIR}/${BUILD_DIR}/fe ; 
                '''
                }
            }
        }
        stage("Build_n_Run") {
            steps {
                catchError {
                sh label: '', script: '''#!/bin/bash
                . /projects/ddm/util/.bashrc.project
                #
                module load bsub2/'v1.1.10' gcc/'7_2_0' visualizer/'2022_2_1' cmake/'3_10_2' dvt/'20_1_8' perl/'5_16_3_RH7' execman/'T_2022_06_SP2_2'  recital/'v2.91.02' onespin360dv/'2021_2_3' questacoreprime/'2023_1' icmanage/'icm.41057' idm_scripts/'2020_11_09' dftp_scripts/'2016_02_11' catapult/'10_2a_769446' designcompiler/'T_2022_03_SP5' designware/'vip_R_2021_03' formality/'T_2022_03_SP5' genus/'191' iccompiler_II/'T_2022_03_SP5' ipxact/'2018_08_20' bcompare/'4_0_7_19761' librarycompiler/'Q_2019_12_SP2' magillem/'5_11_2_0' mipf/'2019_07_10' modularmake/'v1.13.9' nsn_gw/'v4.1' powerartist/'2023_R1_3' powerreplay/'T_2022_06_SP2_3' primepower/'U_2022_12_SP3' primetime/'T_2022_03_SP5' reg_gen/'2019_08_16' socrates/'18_1_0' spyglass/'T_2022_06_1_ufe' vc_static/'T_2022_06_SP1_1' vcsmx/'T_2022_06_SP1' verdi/'T_2022_06_SP1' lsf/'10_1' checklicenses/'1_0'
                module list
                #
                WHOAMI=`whoami` ;
                export WORK_DIR=${WORK_DIR_PREFIX}/${WHOAMI}/${WORK_DIR_MIDFIX_0}/${NODE_NAME}/${WORK_DIR_MIDFIX_1}/${JOB_NAME} ;
                #
                echo '################### '"${REFERENCE_IP_NAME}"' '"${REGRESSION_TYPE}"' regression ###################'
                BUILD_DIR=${REFERENCE_IP_NAME}"_JENKINS_"${BUILD_ID} ;
                export WS_ROOT=${WORK_DIR}/${BUILD_DIR} ;
                export MODULES_PATH=${WORK_DIR}/${BUILD_DIR}/fe ;
                echo -n ' * INFO :: pwd #3 === ' ; pwd ;
                cd ${WS_ROOT} ;
                echo -n ' * INFO :: pwd #4 === ' ; pwd ;
                ######################## speacial operations for each project ################
                cd verif/jesd_uvm/sim ;
                cp Makefile_Jenkins Makefile ;
                ######################## speacial partf for each project ################
                echo -n ' * INFO :: pwd #5 === ' ; pwd ;
                if $BUILD_ID_COVERAGE_ON ; then
                ./vmt run --target=${REGRESSION_TYPE} --sim=QUESTA --verb=UVM_LOW --tasklimit=40 --cov 
                else
                ./vmt run --target=${REGRESSION_TYPE} --sim=QUESTA --verb=UVM_LOW --tasklimit=40                 
                fi
                ./vmt check > vmt_check.log
                cat vmt_check.log
                tmpa=`grep -i "100.00% pass rate" vmt_check.log | wc -l`
                tmpb=`grep -i "FATAL" vmt_check.log | wc -l`
                echo $tmpa
                echo $tmpb
                if [ $tmpa -eq 1 ]; then
                    echo "${REFERENCE_IP_NAME} ${REGRESSION_TYPE} run was successful"
                    exit 0
                else
                    echo "${REFERENCE_IP_NAME} ${REGRESSION_TYPE} run was NOT successful"
                    exit 1
                fi
                '''
                }
            }
        }
		stage("Regression Status Gen") {
			steps {
				catchError {
				sh label: '', script: '''#!/bin/bash
                . /projects/ddm/util/.bashrc.project
                #
                WHOAMI=`whoami` ;
                export WORK_DIR=${WORK_DIR_PREFIX}/${WHOAMI}/${WORK_DIR_MIDFIX_0}/${NODE_NAME}/${WORK_DIR_MIDFIX_1}/${JOB_NAME} ;
                BUILD_DIR=${REFERENCE_IP_NAME}"_JENKINS_"${BUILD_ID} ;
                export WS_ROOT=${WORK_DIR}/${BUILD_DIR} ;
				ln -s ${WS_ROOT} my_sim ;
				'''
				/* no JUNIT support by *mm1* by default
				junit 'my_sim/regression/junit_report.xml'
				*/
				}
			}
		}
		stage("To Artifactory") {
            when {
                environment name: 'BUILD_ID_STORE_ARTIFACTS', value: 'true'
            }
			steps {
				catchError {
				sh label: '', script: '''#!/bin/bash
                . /projects/ddm/util/.bashrc.project
                #
                module load bsub2/'v1.1.10' gcc/'7_2_0' visualizer/'2022_2_1' cmake/'3_10_2' dvt/'20_1_8' perl/'5_16_3_RH7' execman/'T_2022_06_SP2_2'  recital/'v2.91.02' onespin360dv/'2021_2_3' questacoreprime/'2023_1' icmanage/'icm.41057' idm_scripts/'2020_11_09' dftp_scripts/'2016_02_11' catapult/'10_2a_769446' designcompiler/'T_2022_03_SP5' designware/'vip_R_2021_03' formality/'T_2022_03_SP5' genus/'191' iccompiler_II/'T_2022_03_SP5' ipxact/'2018_08_20' bcompare/'4_0_7_19761' librarycompiler/'Q_2019_12_SP2' magillem/'5_11_2_0' mipf/'2019_07_10' modularmake/'v1.13.9' nsn_gw/'v4.1' powerartist/'2023_R1_3' powerreplay/'T_2022_06_SP2_3' primepower/'U_2022_12_SP3' primetime/'T_2022_03_SP5' reg_gen/'2019_08_16' socrates/'18_1_0' spyglass/'T_2022_06_1_ufe' vc_static/'T_2022_06_SP1_1' vcsmx/'T_2022_06_SP1' verdi/'T_2022_06_SP1' lsf/'10_1' checklicenses/'1_0'
                module unload lsf
                module load lsf
                #
                module unload python
                module load python/3_8_0 ;
                #
                module list
                #
                WHOAMI=`whoami` ;
                export WORK_DIR=${WORK_DIR_PREFIX}/${WHOAMI}/${WORK_DIR_MIDFIX_0}/${NODE_NAME}/${WORK_DIR_MIDFIX_1}/${JOB_NAME} ;
                BUILD_DIR=${REFERENCE_IP_NAME}"_JENKINS_"${BUILD_ID} ;
                export WS_ROOT=${WORK_DIR}/${BUILD_DIR} ;
                cd ${WS_ROOT} ;
                # ========================================
                # Merging Regression Test Suite Results
                # ========================================
                DATE=`date +"%Y-%m-%d"` ;
                bsub -Is -q i_soc_rh7 -R "rusage[mem=10000]" vcover -64 merge -testassociated ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/mergefile_${DATE}.ucdb ${PWD}/verif/jesd_uvm/sim/out_cov/*.ucdb
                # ========================================
                # Merging Test Results w/ VPlan
                # ========================================
                #
                case "$RegressionMainCategory" in
                  TX)
                    cmd="bsub -Is -q i_soc_rh7 -R "rusage[mem=10000]" vcover -64 merge 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/mergefile_tx_cov.ucdb 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/mergefile_${DATE}.ucdb  
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/vplan_tx.ucdb" 
                    $cmd ;
                    MERGEFILE=mergefile_tx_cov.ucdb;
                    ;;
                  RX)
                    cmd="bsub -Is -q i_soc_rh7 -R "rusage[mem=10000]" vcover -64 merge  
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/mergefile_rx_cov.ucdb 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/mergefile_${DATE}.ucdb 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/vplan_rx.ucdb"
                    $cmd ;
                    MERGEFILE=mergefile_rx_cov.ucdb;
                    ;;
                  *)
                    cmd="bsub -Is -q i_soc_rh7 -R "rusage[mem=10000]" vcover -64 merge 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/vplan_full.ucdb 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/vplan_tx.ucdb  
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/vplan_rx.ucdb"
                    $cmd ;

                    sleep 5 ;

                    cmd="bsub -Is -q i_soc_rh7 -R "rusage[mem=10000]" vcover -64 merge 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/mergefile_full_cov.ucdb 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/mergefile_${DATE}.ucdb 
                    ${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/vplan_full.ucdb"
                    $cmd ;
                    MERGEFILE=mergefile_full_cov.ucdb;
                    ;;
                esac
                # ================================================================
                # Setting the Necessary Envs for Questa-Collector Runner execution
                # ================================================================
                if $BUILD_ID_COVERAGE_ON ; then
                    export SUFFIX='_coverage' ;
                else
                    export SUFFIX='' ;
                fi
                export CASE=`echo ${REGRESSION_TYPE}${SUFFIX}|  tr [:upper:] [:lower:]` ;
                #
                export DUT=/top/jesd_top ;
                export REPORT_TEMPLATE=${PWD}/verif/jesd_uvm/doc/report_template.json.txt ;
                export COLLECTOR_REPORT_DEST=${PWD}/collector_output_test ;
                export COLLECTOR_REPORT_DEST=https://artifactory-espoo1.int.net.nokia.com/artifactory/soc-reporting-local/robot_test/rautu/soc_verification/jesd/${CASE}
                export UCDB=${PWD}/verif/jesd_uvm/sim/REPORT/ucdbs/${MERGEFILE}
                
                export USOCR_PROGRAM=`echo ${USOCR_PROGRAM}                     | tr [:upper:] [:lower:]` ;
                export USOCR_PROJECT=`echo ${USOCR_PROJECT}                     | tr [:upper:] [:lower:]` ;
                export USOCR_INSTANCE=`echo ${USOCR_INSTANCE}                   | tr [:upper:] [:lower:]` ;
                export USOCR_REGRESSION_TARGET=`echo ${REGRESSION_TYPE}         | tr [:upper:] [:lower:]` ;
                export USOCR_MILESTONE=`echo ${USOCR_MILESTONE}                 | tr [:upper:] [:lower:]` ;

                
                curl --user <USERNAME> https://gerrit.ext.net.nokia.com/gerrit/a/projects/MN%2FSOC%2Fscripts%2Fdfe%2Funified-soc-reporting-misc/branches/master/files/templates%2Freport_metadata_template.json/content | base64 --decode > report_metadata_template.json
                
                curl --user <USERNAME> https://gerrit.ext.net.nokia.com/gerrit/a/projects/MN%2FSOC%2Fscripts%2Fdfe%2Funified-soc-reporting-misc/branches/master/files/scripts%2Funified-soc-reporting-prerequisites.sh/content | base64 --decode > unified-soc-reporting-prerequisites.sh
                . unified-soc-reporting_prerequisites.sh


                envsubst < ${PWD}/${USOCR_PREFS_PATH}/report_metadata_template.json > ${PWD}/${USOCR_PREFS_PATH}/report_metadata.json
                #
                # =================================================================================================
                # -- Artifactory Authentication bounds action - i.e. Jenkins Master side propagation of credentials
                # -- Attn/Hox :: - using the initially cached credentials as a first measure ( as a tmp shortcut )
                # <> JFrog associated cfg content for Artifactory Authentication purposes - created (alrdy earlier)
                # =================================================================================================
                # Executing Questa-Collector Runner and carrying out the Artifactory storage
                # ==========================================================================
                curl http://artifactory-espoo1.ext.net.nokia.com/artifactory/soc-reporting-meta-local/scripts/questa-collector/run.csh | tcsh
                # ==================================================================================================
                # -- desc :: struct to carry out FAILING TEST CASE PREFS collection along with standard report files
                # ==================================================================================================
                #
                # -- NOTE :: 2022-Dec-22 :: commented OFF, as it was instructed _not_ to use Artifactory for below "extended" reporting purposes  
                #         => resolving an alternative storage approach in progress
                #
                #DUMP_DIR=`echo ${BUILD_DIR} | tr [:upper:] [:lower:]` ;
                #export DUMP_DIR='to_artifactory_'${DUMP_DIR} ;
                #
                #cd ${WS_ROOT} ;
                #mkdir -p ${WS_ROOT}/verif/jesd_uvm/sim/${DUMP_DIR} ;
                #
                ## ====================================
                ## OPTION 1/2 :: "as-is dumping"
                ## ====================================
                #
                ## -- local copies
                #cp -p ${WS_ROOT}/verif/jesd_uvm/sim/REPORT/regression_report.txt ${WS_ROOT}/verif/jesd_uvm/sim/${DUMP_DIR}/ ;
                #cp -p ${WS_ROOT}/verif/jesd_uvm/sim/REPORT/vmt.log               ${WS_ROOT}/verif/jesd_uvm/sim/${DUMP_DIR}/ ;
                #
                ## --defining *soctest* binary's location dynamically
                #SOCTEST_BIN=`find ./temp_*/venv/bin -name soctest` ;
                #
                ## -- remote ( Artifactory ) copies
                #${SOCTEST_BIN} copy -v ${WS_ROOT}/verif/jesd_uvm/sim/REPORT/regression_report.txt ${COLLECTOR_REPORT_DEST}/${DUMP_DIR}/ ;
                #${SOCTEST_BIN} copy -v ${WS_ROOT}/verif/jesd_uvm/sim/REPORT/vmt.log               ${COLLECTOR_REPORT_DEST}/${DUMP_DIR}/ ;
                #
				## -- defining the line-up failing testcases dynamically
                #arr_failed=(`cat ${WS_ROOT}/verif/jesd_uvm/sim/REPORT/regression_report.txt | grep 'FAILED' | awk '{print $1}'`) ;
                #
                #for i in "${arr_failed[@]}";
                #do
                #  echo "$i" ;
                #  # -- local copies
                #  mkdir -p ${WS_ROOT}/verif/jesd_uvm/sim/${DUMP_DIR}/$i ;
                #  cp -p    ${WS_ROOT}/verif/jesd_uvm/sim/out_sims/$i/simulation.log   ${WS_ROOT}/verif/jesd_uvm/sim/${DUMP_DIR}/$i/ ;  
                #  # -- remote ( Artifactory ) copies
                #  ${SOCTEST_BIN} copy -v ${WS_ROOT}/verif/jesd_uvm/sim/out_sims/$i/simulation.log    ${COLLECTOR_REPORT_DEST}/${DUMP_DIR}/$i/ ;
                #done
                #
                ## ====================================
                ## OPTION 2/2 :: "ZIP archive dumping" ----- "cost-efficient compressed archive creation"
                ## ====================================
                #
                #${SOCTEST_BIN} archive-create -v -fmt zip ${WS_ROOT}/verif/jesd_uvm/sim/${DUMP_DIR} ${COLLECTOR_REPORT_DEST}
                #

                export DUT=/dut_top/DUT ;
                export REPORT_TEMPLATE=${PWD}/${USOCR_PREFS_PATH}/report_metadata.json ;
                export COLLECTOR_REPORT_DEST=${ARTIFACTORY_ROOT_URL}/soc-reporting-local/${USOCR_PROGRAM}/soc_verification/${USOCR_PROJECT}/${USOCR_INSTANCE}/${REGRESSION_TYPE}/${USOCR_MILESTONE}/
                #export COLLECTOR_REPORT_DEST=${ARTIFACTORY_ROOT_URL}/soc-reporting-local/robot_test/${USOCR_PROGRAM}/soc_verification/${USOCR_PROJECT}/${USOCR_INSTANCE}/${REGRESSION_TYPE}/${USOCR_MILESTONE}/
                export UCDB=${PWD}/${USOCR_PREFS_PATH}/REPORT/ucdbs/${MERGEFILE} ;
                curl ${ARTIFACTORY_ROOT_URL}/soc-reporting-meta-local/scripts/questa-collector/run.csh | tcsh
				

				'''
				}
			}
		}
		stage("To Artifacts") {
			steps {
				catchError {
                archiveArtifacts artifacts: "my_sim/verif/jesd_uvm/sim/comp_logs/* , my_sim/verif/jesd_uvm/sim/out_sims/*/simulation.log", fingerprint: true
				}
			}
		}
    }
}