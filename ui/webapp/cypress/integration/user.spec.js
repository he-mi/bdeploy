describe('Tests related to the current user', function () {
  const currentUserFullName = 'John Doe';
  const currentUserEmail = 'John Doe@example.com';

  const currentUser = 'admin'; // original user as created on setup
  const currentUserPassword = 'admin'; // original password as created on setup

  beforeEach(() => {
    cy.login();
  });

  it('Edits the current user properties', function () {
    cy.visit('/');
    cy.waitUntilContentLoaded();

    cy.pressMainNavTopButton('User Settings');

    cy.inMainNavFlyin('app-settings', () => {
      cy.contains('button', 'Logout').should('exist');

      // two edit cycles below to ensure that there really ARE changes and SAVE enables
      // (in case of repeated execution on the same data during development)

      // 1st change
      cy.get(`app-bd-panel-button[text="Edit User"]`).click();
      cy.get('app-bd-dialog-toolbar[header="Edit User"]').should('exist');
      cy.contains('button', 'SAVE').should('exist').and('be.disabled');

      cy.fillFormInput('fullName', `${currentUserFullName} (*)`);
      cy.fillFormInput('email', `${currentUserEmail} (*)`);
      cy.contains('button', 'SAVE').should('exist').and('be.enabled').click();

      cy.contains('button', 'Logout').should('exist');
      cy.contains('div', 'Full Name:').parent().contains('div', `${currentUserFullName} (*)`).should('exist');
      cy.contains('div', 'E-Mail:').parent().contains('div', `${currentUserEmail} (*)`).should('exist');

      // 2nd change
      cy.get(`app-bd-panel-button[text="Edit User"]`).click();
      cy.get('app-bd-dialog-toolbar[header="Edit User"]').should('exist');
      cy.contains('button', 'SAVE').should('exist').and('be.disabled');

      cy.fillFormInput('fullName', currentUserFullName);
      cy.fillFormInput('email', currentUserEmail);
      cy.contains('button', 'SAVE').should('exist').and('be.enabled').click();

      // finish
      cy.contains('button', 'Logout').should('exist');
      cy.contains('div', 'Full Name:').parent().contains('div', currentUserFullName).should('exist');
      cy.contains('div', 'E-Mail:').parent().contains('div', currentUserEmail).should('exist');

      cy.pressToolbarButton('Close');
    });

    cy.checkMainNavFlyinClosed();
  });

  it('Changes the password', function () {
    cy.visit('/');
    cy.waitUntilContentLoaded();

    cy.pressMainNavTopButton('User Settings');

    cy.inMainNavFlyin('app-settings', () => {
      cy.contains('button', 'Logout').should('exist');

      // two edit cycles below to restore the original password!
      // (in case of repeated execution on the same data during development)
      const newPassword = currentUserPassword + '_CHANGED';

      // 1st change
      cy.get(`app-bd-panel-button[text="Change Password"]`).click();
      cy.get('app-bd-dialog-toolbar[header="Change Password"]').should('exist');
      cy.contains('button', 'SAVE').should('exist').and('be.disabled');

      cy.fillFormInput('passOrig', currentUserPassword);
      cy.fillFormInput('passNew', newPassword);
      cy.contains('button', 'SAVE').should('exist').and('be.disabled');
      cy.fillFormInput('passVerify', newPassword);
      cy.contains('button', 'SAVE').should('exist').and('be.enabled').click();

      // 2nd change
      cy.get(`app-bd-panel-button[text="Change Password"]`).click();
      cy.get('app-bd-dialog-toolbar[header="Change Password"]').should('exist');
      cy.contains('button', 'SAVE').should('exist').and('be.disabled');

      cy.fillFormInput('passOrig', newPassword);
      cy.fillFormInput('passNew', currentUserPassword);
      cy.contains('button', 'SAVE').should('exist').and('be.disabled');
      cy.fillFormInput('passVerify', currentUserPassword);
      cy.contains('button', 'SAVE').should('exist').and('be.enabled').click();

      cy.get('app-bd-dialog-toolbar[header="User Settings"]').should('exist');
      cy.pressToolbarButton('Close');
    });

    cy.checkMainNavFlyinClosed();
  });

  it('Checks the token page', function () {
    cy.visit('/');
    cy.waitUntilContentLoaded();

    cy.pressMainNavTopButton('User Settings');

    cy.inMainNavFlyin('app-settings', () => {
      cy.contains('button', 'Logout').should('exist');

      cy.get('app-bd-panel-button[text="Create Token(s)"]').click();
      cy.get('app-token').should('exist');

      cy.get('textarea')
        .invoke('val')
        .then((text) => {
          expect(text.length > 500 && text.length < 1000);
        });

      cy.fillFormToggle('genFull');

      cy.get('textarea')
        .invoke('val')
        .then((text) => {
          expect(text.length > 2000);
        });
      cy.get('app-bd-panel-button[text="Back to overview"]').click();

      cy.get('app-bd-dialog-toolbar[header="User Settings"]').should('exist');
      cy.pressToolbarButton('Close');
    });

    cy.checkMainNavFlyinClosed();
  });

  it('Changes the theme', function () {
    cy.visit('/');
    cy.waitUntilContentLoaded();

    // check default
    cy.get('body').should('have.class', 'app-light-theme');

    cy.pressMainNavTopButton('Select Theme');

    cy.contains('div', 'Light / Yellow').should('exist').click();
    cy.get('body').should('have.class', 'app-light-yellow-theme');

    cy.contains('div', 'Dark / Blue').should('exist').click();
    cy.get('body').should('have.class', 'app-dark-theme');

    cy.contains('div', 'Dark / Yellow').should('exist').click();
    cy.get('body').should('have.class', 'app-dark-yellow-theme');

    cy.contains('div', 'Light / Blue').should('exist').click();
    cy.get('body').should('have.class', 'app-light-theme');
  });
});