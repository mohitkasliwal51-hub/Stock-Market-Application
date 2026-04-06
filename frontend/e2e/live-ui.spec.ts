import { expect, test } from '@playwright/test';

test.describe('Live UI smoke checks', () => {
  test('home page renders the application shell', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByRole('heading', { name: 'Welcome to the Stock Market Application' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Stock Market' })).toBeVisible();
  });

  test('login route renders the login form', async ({ page }) => {
    await page.goto('/login');

    await expect(page.getByRole('heading', { name: 'Login' })).toBeVisible();
    await expect(page.getByPlaceholder('Enter Username')).toBeVisible();
    await expect(page.getByPlaceholder('Password')).toBeVisible();
  });

  test('signup route renders the signup form', async ({ page }) => {
    await page.goto('/signup');

    await expect(page.getByRole('heading', { name: 'Join the Community' })).toBeVisible();
    await expect(page.getByPlaceholder('Enter username')).toBeVisible();
    await expect(page.getByPlaceholder('Enter Email')).toBeVisible();
  });

  test('unknown route renders the not found page', async ({ page }) => {
    await page.goto('/this-route-does-not-exist');

    await expect(page.getByRole('heading', { name: 'The page you are looking for, does not exist' })).toBeVisible();
  });
});