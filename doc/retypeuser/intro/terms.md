---
order: 1
icon: info
---

<style>
    .t1 td {
        vertical-align: text-top;
    }
    .t1 th:first-child {
        width: 25%;
    }
</style>

# Terms

BDeploy has some important terms and concepts which should be known before diving into the details of the application itself. These terms will be used all over the documentation to cross-reference other parts of the application.

Term | Meaning
--- | ---
**Instance Group** | An **Instance Group** is the main top-level element used in **BDeploy**. As the name suggests, it provides a grouping of **Instances**.
**Product** | A piece of software that can be deployed with **BDeploy**. A product is a bundle of **Applications** along with some meta-information.
**Application** | A part of a **Product** that can be started and stopped separately. Applications either run as a **Process** on a server machine or as a **Client Application** on an end user device.
**Process** | A server-side **Application** that is configured to be started on one of the server **Nodes** of an **Instance**.
**Client Application** | An **Application** that can be distributed on end user devices.
**Instance** | An **Instance** is the actual deployment configuration of a **Product**. In one instance, the **Applications** of a **Product** can be distributed as **Processes** on one or more **Nodes**.<br/><br/>The **Product** (and it's **Applications**) describes the _possible_ configuration values (e.g. configuration files, command line parameters, etc.) - the **Instance** provides _actual_ values for these.
**Instance Version** | An **Instance Version** describes the configuration state of an **Instance** at a given time. Each change that is done by the user results in a new instance version. Versions are immutable and cannot be changed. Each new change results in a new version of an **Instance**.
**Build** | A **Build** represents the immutable collection of all runtime artifacts (binary files, configuration files, templates etc.) of a **Product**. As a **Build** is immutable, the build tool (e.g. Maven, Gradle, Eclipse, etc.) creates a new one on every execution.
**Product Version** | A **Product Version** represents a **Build** that is marked with a **Tag**.
**Tag** | A **Tag** identifies a **Product Version**. The structure of a tag is defined by the **Product** itself. The only obligation is that a tag has to be unique for different **Builds** of the same **Product**.
**Node** | A server machine (virtual machine or bare metal) that is used in an **Instance** to run the configured **Processes** on it. **BDeploy** supports Windows and Linux nodes. They may even be mixed within one **Instance**.
**Minion** | Agent process provided by **BDeploy** that has to run on every **physical node** used by BDeploy. In a multi-node setup one minion takes the role of a master.
**Deployment** | The process of getting one **Instance Version** up and running. This includes the major process steps: **Installation**, **Activation**, **Process Control**.
**Installation** | The process of transferring the configured **Processes** of an **Instance Version** to all **Nodes** of that **Instance**. The **Installation** makes this **Instance Version** available for its **Activation**.
**Activation** | The **Activation** marks a previously **installed** **Instance Version** as the one to be started by the **Process Control**. Only one instance version can be active for one instance at the same time.
**Process Control** | The **Process Control** is responsible for starting, keep-alive and stopping the **Processes** of the active **Instance Version**.
**System** | A **System** is a bracket around multiple **Instances** using potentially different **Products**. A **System** is a logical group of individual pieces of deployed software.
